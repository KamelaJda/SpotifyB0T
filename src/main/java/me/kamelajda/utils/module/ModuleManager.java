package me.kamelajda.utils.module;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ModuleManager {

    public final Set<IModule> modules = new HashSet<>();

    public void loadModule(IModule module) {
        if (modules.stream().filter(f -> f.getName().equals(module.getName())).count() > 1) {
            log.error("Module with name " + module.getName() + " already exist");
            return;
        }

        log.info("Loading module " + module.getName() + "...");
        modules.add(module);

        try {
            module.startUp();
            log.info("Module " + module.getName() + " is successfully loaded!");
        } catch (Exception e) {
            log.error("An error occurred while enabling module: " + module.getName(), e);
        }
    }

}
