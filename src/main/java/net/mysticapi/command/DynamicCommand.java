package net.mysticapi.command;

import net.mysticapi.command.annotation.Command;
import net.mysticapi.command.annotation.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

class DynamicCommand extends org.bukkit.command.Command {

    private final CommandBase instance;
    private final Command commandAnnotation;
    private final Method defaultMethod;
    private final boolean defaultPlayerOnly;
    private final List<SubCommandEntry> subCommands;

    DynamicCommand(String name, CommandBase instance, Command commandAnnotation,
                   Method defaultMethod, boolean defaultPlayerOnly,
                   List<SubCommandEntry> subCommands) {
        super(name, commandAnnotation.description(), commandAnnotation.usage(),
                List.of(commandAnnotation.aliases()));
        this.instance = instance;
        this.commandAnnotation = commandAnnotation;
        this.defaultMethod = defaultMethod;
        this.defaultPlayerOnly = defaultPlayerOnly;
        this.subCommands = subCommands;

        if (!commandAnnotation.permission().isEmpty()) {
            setPermission(commandAnnotation.permission());
        }
        if (!commandAnnotation.permissionMessage().isEmpty()) {
            setPermissionMessage(commandAnnotation.permissionMessage());
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermissionSilent(sender)) {
            sender.sendMessage(getPermissionMessage() != null
                    ? getPermissionMessage()
                    : "§cJe hebt geen toestemming om dit commando te gebruiken.");
            return true;
        }

        if (args.length > 0) {
            String subName = args[0];
            SubCommandEntry entry = findSubCommand(subName);
            if (entry != null) {
                if (!entry.permission().isEmpty() && !sender.hasPermission(entry.permission())) {
                    sender.sendMessage(commandAnnotation.permissionMessage());
                    return true;
                }
                if (entry.playerOnly() && !(sender instanceof Player)) {
                    sender.sendMessage("§cDit commando kan alleen door een speler gebruikt worden.");
                    return true;
                }

                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                invoke(entry.method(), sender, label, subArgs);
                return true;
            }
        }

        if (defaultMethod != null) {
            if (defaultPlayerOnly && !(sender instanceof Player)) {
                sender.sendMessage("§cDit commando kan alleen door een speler gebruikt worden.");
                return true;
            }
            invoke(defaultMethod, sender, label, args);
            return true;
        }

        sender.sendMessage("§cGebruik: " + getUsage());
        return true;
    }

    private SubCommandEntry findSubCommand(String name) {
        for (SubCommandEntry entry : subCommands) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
            for (String alias : entry.aliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private void invoke(Method method, CommandSender sender, String label, String[] args) {
        try {
            method.setAccessible(true);
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0] == CommandContext.class) {
                method.invoke(instance, new CommandContext(sender, label, args));
            } else {
                method.invoke(instance, sender, args);
            }
        } catch (ReflectiveOperationException exception) {
            sender.sendMessage("§cEr is een fout opgetreden bij het uitvoeren van dit commando.");
            exception.printStackTrace();
        }
    }

    record SubCommandEntry(String name, String[] aliases, String permission, boolean playerOnly, Method method) {
    }
}
