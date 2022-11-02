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

import me.kamelajda.services.SpotifyService;
import me.kamelajda.utils.commands.ICommand;
import me.kamelajda.utils.commands.SlashContext;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class RefreshCommand extends ICommand {

    private final SpotifyService spotifyService;

    public RefreshCommand(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
        name = "refresh";
        requiredPermissions = DefaultMemberPermissions.DISABLED;
        usage = "[artistid:string]";
    }

    @Override
    protected void execute(SlashContext context) {
        context.getEvent().deferReply(true).queue();

        ApplicationInfo complete = context.getShardManager().retrieveApplicationInfo().complete();

        if (complete.getTeam().getMemberById(context.getUser().getIdLong()) == null) {
            context.sendTranslate("refresh.nonperm");
            return;
        }

        context.send("Rozpoczynam sprawdzaenie!");

        OptionMapping id = context.getEvent().getOption("artistid");

        if (id != null) spotifyService.check(id.getAsString());
        else spotifyService.check(null);
    }

}
