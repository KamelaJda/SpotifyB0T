package me.kamelajda.modules.commands.commands;

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
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ArtistsCommand extends ICommand {

    private final SubscribeArtistService subscribeArtistService;
    private final EventWaiter eventWaiter;

    public ArtistsCommand(SubscribeArtistService subscribeArtistService, EventWaiter eventWaiter) {
        this.subscribeArtistService = subscribeArtistService;
        this.eventWaiter = eventWaiter;
        name = "artists";
        category = CommandCategory.BASIC;
        commandData = getData();
    }

    @Override
    protected boolean execute(SlashContext context) {
        List<ArtistInfo> list = subscribeArtistService.getAllArtist(context.getUser().getIdLong());

        if (list == null || list.isEmpty()) {
            context.getEvent().deferReply(true).queue();
            context.sendTranslate("artists.empty");
            return false;
        }

        context.getEvent().deferReply(false).complete();
        context.getHook().editOriginal(context.getLanguage().get("global.generic.loading")).queue();

        List<EmbedBuilder> pages = list.stream().map(m -> embed(context.getLanguage(), context.getMember(), m)).collect(Collectors.toList());

        EmbedPaginator.create(pages, context.getUser(), eventWaiter, context.getHook());

        return true;
    }

    public static EmbedBuilder embed(Language language, @Nullable Member member, ArtistInfo artistInfo) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(artistInfo.getDisplayName(), artistInfo.getLink());
        eb.setImage(artistInfo.getThumbnailUrl());

        if (artistInfo.getLastAlbumName() != null) {
            eb.addField(language.get("artists.last.album"), String.format("[%s](%s)", artistInfo.getLastAlbumName(), artistInfo.getLastAlbumLink()), false);
            eb.addField(language.get("artist.last.album.release.date"), artistInfo.getLastAlbumDate(), false);
        }

        if (member != null) eb.setColor(UserUtil.getColor(member));
        else eb.setColor(Color.GREEN);

        return eb;
    }

}
