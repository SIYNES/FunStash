package ru.siynes.funstash.api.model;

import lombok.Value;
import org.bukkit.Location;

@Value
public class StashLocationKey {
    String world;
    int x;
    int y;
    int z;

    public static StashLocationKey from(Location location) {
        return new StashLocationKey(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}