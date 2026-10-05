package ru.siynes.funstash.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.siynes.funstash.FunStash;
import ru.siynes.funstash.api.command.CommandManager;
import ru.siynes.funstash.commands.impl.StashGiveSub;
import ru.siynes.funstash.configs.TranslationConfig;
import ru.siynes.funstash.utils.ColorUtil;

import java.util.Arrays;
import java.util.List;
public class FunStashCommand extends CommandManager {
    private final TranslationConfig config;

    public FunStashCommand() {
        addSub(new StashGiveSub(), new String[]{"give"}, new Permission("funstash.give"));
        config = FunStash.getInstance().getTranslationConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return true;

        final SubCommandWrapper wrapper = getWrapperFromLabel(args[0]);

        if (wrapper != null) {
            if (!sender.hasPermission(wrapper.getPermission())) {
                sender.sendMessage(ColorUtil.translate(config.getNoPermissions()));
                return true;
            }

            wrapper.getCommand().onExecute(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return getAliases();
        }
        final SubCommandWrapper wrapper = getWrapperFromLabel(args[0]);
        if (wrapper == null) return null;

        return wrapper.getCommand().onTabComplete(sender, args);
    }
}
