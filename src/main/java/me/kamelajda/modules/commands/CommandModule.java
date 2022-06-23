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

package me.kamelajda.modules.commands;

import com.google.common.eventbus.EventBus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kamelajda.modules.commands.commands.*;
import me.kamelajda.modules.commands.listener.ExecuteCommandAsButtonListener;
import me.kamelajda.redis.services.RedisStateService;
import me.kamelajda.services.GuildConfigService;
import me.kamelajda.services.SpotifyService;
import me.kamelajda.services.SubscribeArtistService;
import me.kamelajda.services.UserConfigService;
import me.kamelajda.utils.EventWaiter;
import me.kamelajda.utils.commands.CommandManager;
import me.kamelajda.utils.language.LanguageService;
import me.kamelajda.utils.module.IModule;

@RequiredArgsConstructor
@Getter
public class CommandModule extends IModule {

    private final String name = "commands";

    private final CommandManager commandManager;
    private final SubscribeArtistService subscribeArtistService;
    private final SpotifyService spotifyService;
    private final EventWaiter eventWaiter;
    private final EventBus eventBus;
    private final GuildConfigService guildConfigService;
    private final UserConfigService userConfigService;
    private final LanguageService languageService;
    private final RedisStateService redisStateService;

    @Override
    public void startUp() {
        commandManager.registerCommand(new SubscribeCommand(subscribeArtistService, spotifyService, eventWaiter, redisStateService));
        commandManager.registerCommand(new HelpCommand(commandManager));
        commandManager.registerCommand(new ArtistsCommand(subscribeArtistService, eventWaiter));
        commandManager.registerCommand(new ConfigureGuildCommand(guildConfigService));
        commandManager.registerCommand(new ConfigureUserCommand(userConfigService));
        commandManager.registerCommand(new ReportBugCommand());
        commandManager.registerCommand(new ContributorsCommand(eventWaiter));

        registerListener(new ExecuteCommandAsButtonListener(subscribeArtistService, eventWaiter, userConfigService, languageService, guildConfigService));
    }

    @Override
    public void disable() {
        this.unregisterListeners();
    }

}
