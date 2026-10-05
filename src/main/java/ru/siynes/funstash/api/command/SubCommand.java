package ru.siynes.funstash.api.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    void onExecute(CommandSender sender, String[] args);

    List<String> onTabComplete(CommandSender sender, String[] args);
}
