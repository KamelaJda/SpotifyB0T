package me.kamelajda.utils.commands;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kamelajda.utils.Static;
import me.kamelajda.utils.enums.CommandCategory;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public abstract class ICommand {

    protected String name;
    protected CommandCategory category;
    protected CommandDataImpl commandData = null;

    protected final Map<String, Method> subCommands = new HashMap<>();

    public void preExecute(SlashContext context) {
        execute(context);
    }

    protected boolean execute(SlashContext context) {
        throw new UnsupportedOperationException("Komenda nie ma zaimplementowanej funkcji execute(SlashContext)");
    }

    @Override
    public String toString() {
        return this.name;
    }

    protected CommandDataImpl getData() {
        return new CommandDataImpl(this.name, Static.defualtLanguage.get(this.name + ".description"));
    }

}
