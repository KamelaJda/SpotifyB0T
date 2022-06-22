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

package me.kamelajda.utils;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class EmbedPaginator {

    private static final String FIRST_ID = "FIRST";
    private static final String RIGHT_ID = "RIGHT";
    private static final String LEFT_ID = "LEFT";
    private static final String LAST_ID = "LAST";
    private static final String STOP_ID = "STOP";

    public static final Button FIRST_BUTTON = Button.secondary(FIRST_ID, Emoji.fromUnicode("\u23EE"));
    public static final Button LEFT_BUTTON = Button.primary(LEFT_ID, Emoji.fromUnicode("\u25C0"));
    public static final Button RIGHT_BUTTON = Button.primary(RIGHT_ID, Emoji.fromUnicode("\u25B6"));
    public static final Button LAST_BUTTON = Button.secondary(LAST_ID, Emoji.fromUnicode("\u23ED"));
    public static final Button STOP_BUTTON = Button.danger(STOP_ID, Emoji.fromUnicode("\u23F9"));

    private final EventWaiter eventWaiter;
    @Getter private final List<EmbedBuilder> pages;
    private final long userId;
    private final int second;
    @Getter private final InteractionHook interaction;
    private final ActionRow customActionRow;
    private final BiConsumer<EmbedPaginator, ButtonInteractionEvent> eventAction;

    @Getter @Setter
    private int thisPage = 1;

    public static EmbedPaginator create(List<EmbedBuilder> pages, User user, EventWaiter eventWaiter, InteractionHook interaction, ActionRow customActionRow, BiConsumer<EmbedPaginator, ButtonInteractionEvent> eventAction) {
        return new EmbedPaginator(pages, user, eventWaiter, interaction, 60, customActionRow, eventAction);
    }

    private EmbedPaginator(List<EmbedBuilder> pages, User user, EventWaiter eventWaiter, InteractionHook interaction, int second, ActionRow customActionRow, BiConsumer<EmbedPaginator, ButtonInteractionEvent> eventAction) {
        this.eventWaiter = eventWaiter;
        this.pages = pages;
        this.userId = user.getIdLong();
        this.interaction = interaction;
        this.second = second;
        this.customActionRow = customActionRow;
        this.eventAction = eventAction;
        start();
    }

    public static EmbedPaginator create(List<EmbedBuilder> pages, User user, EventWaiter eventWaiter, InteractionHook hook) {
        return new EmbedPaginator(pages, user, eventWaiter, hook, 60, null, null);
    }

    private void start() {
        MessageBuilder builder = new MessageBuilder();
        builder.setEmbeds(render(1));
        builder.setActionRows(getActionRow(1));

        interaction.editOriginal(builder.build())
            .queue(msg -> waitForReaction());
    }

    private void waitForReaction() {
        eventWaiter.waitForEvent(ButtonInteractionEvent.class, this::check, this::handle,
            second, TimeUnit.SECONDS, this::clear);
    }

    private void handle(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        if (customActionRow != null && customActionRow.getActionComponents().stream().map(ActionComponent::getId).collect(Collectors.toSet()).contains(event.getComponentId())) {
            eventAction.accept(this, event);
        } else {
            switch (event.getComponentId()) {
                case FIRST_ID:
                    thisPage = 1;
                    break;
                case LEFT_ID:
                    if (thisPage > 1) thisPage--;
                    break;
                case RIGHT_ID:
                    if (thisPage < pages.size()) thisPage++;
                    break;
                case LAST_ID:
                    thisPage = pages.size();
                    break;
                case STOP_ID:
                    clear();
                    return;
                default:
                    break;
            }
        }

        if (thisPage == -1) return;

        MessageBuilder mb = new MessageBuilder();
        mb.setEmbeds(render(thisPage));
        mb.setActionRows(getActionRow(thisPage));

        event.getHook().editOriginal(mb.build()).queue();
        waitForReaction();
    }

    public boolean check(ButtonInteractionEvent event) {
        if (customActionRow != null && customActionRow.getActionComponents().stream().map(ActionComponent::getId).collect(Collectors.toSet()).contains(event.getComponentId())) return true;

        if (event.getMessageId().equals(interaction.retrieveOriginal().complete().getId()) && event.getUser().getIdLong() == userId) {
            switch (event.getComponentId()) {
                case FIRST_ID:
                case LEFT_ID:
                case RIGHT_ID:
                case LAST_ID:
                case STOP_ID: return true;
                default: return false;
            }
        }
        return false;
    }

    public void clear() {
        setThisPage(-1);
        interaction.editOriginalComponents(Collections.emptyList()).queue();
    }

    private MessageEmbed render(int page) {
        EmbedBuilder pageEmbed = pages.get(page - 1);
        pageEmbed.setFooter(String.format("%s/%s", page, pages.size()), null);
        return pageEmbed.build();
    }

    public List<ActionRow> getActionRow(int page) {
        if (customActionRow != null) return List.of(getActionRow(page, pages), customActionRow);
        return List.of(getActionRow(page, pages));
    }

    public static ActionRow getActionRow(int page, List<?> pages) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(FIRST_BUTTON.withDisabled(page == 1));
        buttons.add(LEFT_BUTTON.withLabel(page == 1 ? "-" : page - 1 + "").withDisabled(page == 1));
        buttons.add(RIGHT_BUTTON.withLabel(pages.size() == page ? "-" : page + 1 + "").withDisabled(pages.size() == page));
        buttons.add(LAST_BUTTON.withDisabled(pages.size() == page));
        buttons.add(STOP_BUTTON);
        return ActionRow.of(buttons);
    }

}
