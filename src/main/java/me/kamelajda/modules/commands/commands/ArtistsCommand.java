/*
 *
 * Copyright 2022 SpotifyB0T
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.kamelajda.modules.commands.commands;

import me.kamelajda.jpa.models.ArtistCreation;
import me.kamelajda.jpa.models.ArtistInfo;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.utils.EmbedPaginator;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.UserUtil;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import me.kamelajda.utils.enums.CommandCategory;
import me.kamelajda.utils.language.Language;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArtistsCommand extends ICommand {

    private static final ButtonImpl DELETE_BUTTON_PUBLIC = new ButtonImpl("delete-artist-server", "artist.remove.from.subs", ButtonStyle.DANGER, null, false,Emoji.fromUnicode("\uD83D\uDDD1️"));
    private static final ButtonImpl DELETE_BUTTON_PRIVATE = new ButtonImpl("delete-artist-private", "artist.remove.from.subs", ButtonStyle.DANGER, null, false,Emoji.fromUnicode("\uD83D\uDDD1️"));

    private final SubscribeArtistService subscribeArtistService;
    private final EventWaiter eventWaiter;

    public ArtistsCommand(SubscribeArtistService subscribeArtistService, EventWaiter eventWaiter) {
        this.subscribeArtistService = subscribeArtistService;
        this.eventWaiter = eventWaiter;
        name = "artists";
        category = CommandCategory.BASIC;
        commandData = getData()
            .addOptions(
                new OptionData(OptionType.STRING, "type", "View your subscriptions or server subscriptions")
                    .addChoice("Private (default value)", SubscribeCommand.SubscribeType.PRIVATE.name())
                    .addChoice("For server", SubscribeCommand.SubscribeType.SERVER.name())
                    .setRequired(false)
            );
    }

    @Override
    protected boolean execute(SlashContext context) {
        SubscribeCommand.SubscribeType type = SubscribeCommand.SubscribeType.valueOf(context.getEvent().getOption("type", SubscribeCommand.SubscribeType.PRIVATE.name(), OptionMapping::getAsString));

        List<ArtistInfo> list;

        context.getEvent().deferReply(false).complete();

        if (type == SubscribeCommand.SubscribeType.PRIVATE || type == SubscribeCommand.SubscribeType.SERVER && !context.getEvent().isFromGuild()) {
            list = subscribeArtistService.getAllArtist(context.getUserConfig());
        } else {
            list = subscribeArtistService.getAllArtist(context.getGuildConfig());
        }

        if (list == null || list.isEmpty()) {
            context.getHook().editOriginal(context.getLanguage().get("artists.empty")).queue();
            return false;
        }

        context.getHook().editOriginal(context.getLanguage().get("global.generic.loading")).queue();

        boolean forServer = type == SubscribeCommand.SubscribeType.SERVER && context.getGuild() != null;
        String name = forServer ? context.getGuild().getName() : context.getUser().getAsMention();

        List<EmbedBuilder> pages = list.stream()
            .map(m -> embed(context.getLanguage(), context.getMember(), m, type, name))
            .collect(Collectors.toList());

        EmbedPaginator.create(pages, context.getUser(), eventWaiter, context.getHook(), formatButton(context.getLanguage(), forServer), consumer(subscribeArtistService, context.getUser()));

        return true;
    }

    public static EmbedBuilder embed(Language language, @Nullable Member member, ArtistInfo artistInfo, SubscribeCommand.SubscribeType type, String name) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(language.get("artists.embed.description." + type.name().toLowerCase(), name));

        eb.setTitle(artistInfo.getDisplayName(), artistInfo.getLink());
        eb.setImage(artistInfo.getThumbnailUrl());

        if (artistInfo.getLastAlbum() != null) {
            eb.addField(field(language, artistInfo, ArtistInfo::getLastAlbum));
        }

        if (artistInfo.getLastTrack() != null) {
            eb.addField(field(language, artistInfo, ArtistInfo::getLastTrack));
        }

        if (artistInfo.getLastFeat() != null) {
            eb.addField(field(language, artistInfo, ArtistInfo::getLastFeat));
        }

        if (member != null) eb.setColor(UserUtil.getColor(member));
        else eb.setColor(Color.GREEN);

        return eb;
    }

    private static MessageEmbed.Field field(Language lang, ArtistInfo info, Function<ArtistInfo, ArtistCreation> fun) {
        ArtistCreation apply = fun.apply(info);

        return new MessageEmbed.Field(
            lang.get("artists.last." + apply.getCreationType().name().toLowerCase()),
            String.format("[%s](%s) - %s", apply.getName(), apply.getLink(), apply.getDate()),
            false
        );
    }

    public static BiConsumer<EmbedPaginator, ButtonInteractionEvent> consumer(SubscribeArtistService subscribeArtistService, User user) {
        return (embedPaginator, event) -> {
            EmbedBuilder builder = embedPaginator.getPages().get(embedPaginator.getThisPage() - 1);
            embedPaginator.getPages().remove(builder);
            String[] s = Objects.requireNonNull(builder.build().getUrl()).split("/");

            Interaction interaction = embedPaginator.getInteraction().getInteraction();

            if (event.getComponentId().endsWith("-server")) {
                Member id = interaction.getGuild().getMemberById(user.getIdLong());
                if (id == null || !id.hasPermission(Permission.MANAGE_SERVER)) return;

                subscribeArtistService.removeArtistForGuild(interaction.getGuild().getIdLong(), s[s.length - 1]);
            } else subscribeArtistService.removeArtist(user.getIdLong(), s[s.length - 1]);

            if (embedPaginator.getPages().isEmpty()) {
                embedPaginator.clear();
                embedPaginator.setThisPage(-1);
            } else embedPaginator.setThisPage(1);
        };
    }

    public static ActionRow formatButton(Language language, boolean forServer) {
        ButtonImpl bt = forServer ? DELETE_BUTTON_PUBLIC : DELETE_BUTTON_PRIVATE;
        return ActionRow.of(bt.withLabel(language.get(bt.getLabel())));
    }

}
