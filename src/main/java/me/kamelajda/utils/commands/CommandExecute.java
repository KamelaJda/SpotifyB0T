package me.kamelajda.utils.commands;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.modules.commands.commands.HelpCommand;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.UsageException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class CommandExecute {

    private final CommandManager commandManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    @Subscribe
    public void onSlashCommand(SlashCommandInteractionEvent e) {
        String channelId = e.getGuild() == null ? "dm" : e.getGuild().getId();

        Runnable run = () -> {
            Thread.currentThread().setName(e.getUser().getId() + "-" + e.getName() + "-" + channelId);
            ICommand c = commandManager.commands.get(e.getName());
            if (c != null && c.getCommandData() != null) {
                SlashContext context = new SlashContext(e, "/", c, Static.defualtLanguage);

                try {
                    c.preExecute(context);
                } catch (UsageException ex) {
                    EmbedBuilder embed = e.isFromGuild() ? HelpCommand.embed(c, e.getMember(), Static.defualtLanguage) : HelpCommand.embed(c, Static.defualtLanguage);
                    context.getHook().sendMessageEmbeds(embed.build()).complete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    context.getHook().sendMessage(Static.defualtLanguage.get("global.command.error")).complete();
                }
            }
        };

        executor.execute(run);
    }

}
