package ru.siynes.funstash.api.model;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class LootItem {
    private final ItemStack itemStack;
    private final double chance;
    private final int amount;
}