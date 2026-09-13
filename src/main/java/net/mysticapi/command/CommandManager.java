package net.mysticapi.command;

import net.mysticapi.command.annotation.Command;
import net.mysticapi.command.annotation.Default;
import net.mysticapi.command.annotation.SubCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final JavaPlugin plugin;
    private final CommandMap commandMap;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = resolveCommandMap();
    }

    public void register(CommandBase commandBase) {
        Class<?> clazz = commandBase.getClass();
        Command commandAnnotation = clazz.getAnnotation(Command.class);
        if (commandAnnotation == null) {
            throw new IllegalStateException("De klasse " + clazz.getName()
                    + " mist de @Command annotatie.");
        }

        Method defaultMethod = null;
        boolean defaultPlayerOnly = false;
        List<DynamicCommand.SubCommandEntry> subCommands = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            SubCommand subCommand = method.getAnnotation(SubCommand.class);
            Default defaultAnnotation = method.getAnnotation(Default.class);

            if (subCommand != null) {
                subCommands.add(new DynamicCommand.SubCommandEntry(
                        subCommand.name(),
                        subCommand.aliases(),
                        subCommand.permission(),
                        subCommand.playerOnly(),
                        method
                ));
            } else if (defaultAnnotation != null) {
                defaultMethod = method;
                defaultPlayerOnly = defaultAnnotation.playerOnly();
            }
        }

        DynamicCommand dynamicCommand = new DynamicCommand(
                commandAnnotation.name(),
                commandBase,
                commandAnnotation,
                defaultMethod,
                defaultPlayerOnly,
                subCommands
        );

        commandMap.register(plugin.getName().toLowerCase(), dynamicCommand);
    }

    public void registerAll(CommandBase... commandBases) {
        for (CommandBase commandBase : commandBases) {
            register(commandBase);
        }
    }

    private CommandMap resolveCommandMap() {
        try {
            Field field = plugin.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(plugin.getServer());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Kon de CommandMap niet ophalen.", exception);
        }
    }
}
