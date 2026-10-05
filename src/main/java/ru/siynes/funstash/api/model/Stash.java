package ru.siynes.funstash.api.model;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
public class Stash {
    private String name;
    private Location location;
    private boolean isActive;
    private List<LootItem> lootItems;
    private ItemStack stashKey;
    private HologramSettings hologram;
}