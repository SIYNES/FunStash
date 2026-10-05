package ru.siynes.funstash.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.siynes.funstash.FunStash;
import ru.siynes.funstash.api.command.SubCommand;
import ru.siynes.funstash.api.model.Stash;
import ru.siynes.funstash.configs.TranslationConfig;
import ru.siynes.funstash.manager.StashManager;
import ru.siynes.funstash.utils.ColorUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StashGiveSub implements SubCommand {
    private final StashManager stashManager;
    private final TranslationConfig config;

    public StashGiveSub() {
        stashManager = FunStash.getInstance().getStashManager();
        config = FunStash.getInstance().getTranslationConfig();
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        if (args.length < 3) return;

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(ColorUtil.translate(config.getPlayerNotFound()));
            return;
        }

        Stash stash = stashManager.getByName(args[1]);

        if (stash == null) {
            sender.sendMessage(ColorUtil.translate(config.getStashNotFound()
                            .replace("{stash}", args[1])));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);

            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtil.translate(config.getNotFormat()));
            return;
        }

        ItemStack key = stash.getStashKey().clone();
        key.setAmount(amount);
        player.getInventory().addItem(key).forEach((slot, item) ->
                player.getWorld().dropItemNaturally(player.getLocation(), item)
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            return stashManager.getAll()
                    .stream()
                    .map(s -> s.getName())
                    .collect(Collectors.toList());
        }

        if (args.length == 4) {
            return Arrays.asList("количество");
        }

        return List.of();
    }
}