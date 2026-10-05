package ru.siynes.funstash.manager;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.siynes.funstash.FunStash;
import ru.siynes.funstash.api.model.HologramSettings;
import ru.siynes.funstash.api.model.Stash;
import ru.siynes.funstash.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class StashHologramManager {
    private final Set<String> itemHolograms = new HashSet<>();

    public void showAll(Collection<Stash> stashes) {
        stashes.forEach(this::show);
    }

    public void removeAll(Collection<Stash> stashes) {
        stashes.forEach(this::hide);
        removeItemHolograms();
    }

    public void show(Stash stash) {
        hide(stash);

        HologramSettings settings = stash.getHologram();
        if (settings == null || !settings.enabled() || settings.lines().isEmpty()) return;

        Location base = stash.getLocation();
        if (base == null || base.getWorld() == null) return;

        Location location = new Location(
                base.getWorld(),
                base.getBlockX() + settings.offsetX(),
                base.getBlockY() + settings.offsetY(),
                base.getBlockZ() + settings.offsetZ()
        );

        List<String> lines = settings.lines().stream()
                .map(ColorUtil::translate)
                .collect(Collectors.toList());

        DHAPI.createHologram("funstash_idle_" + stash.getName().toLowerCase(), location, false, lines);
    }

    public void hide(Stash stash) {
        Hologram hologram = DHAPI.getHologram("funstash_idle_" + stash.getName().toLowerCase());
        if (hologram != null) {
            hologram.delete();
        }
    }

    public void createHologram(Location location, ItemStack item) {
        String name = "funstash_" + System.nanoTime();
        itemHolograms.add(name);

        Hologram hologram = DHAPI.createHologram(name, location);
        DHAPI.addHologramLine(hologram, item.getItemMeta().getDisplayName().replace('§', '&'));
        DHAPI.addHologramLine(hologram, item.getType());

        new BukkitRunnable() {
            @Override
            public void run() {
                itemHolograms.remove(name);
                Hologram current = DHAPI.getHologram(name);
                if (current != null) {
                    current.delete();
                }
            }
        }.runTaskLater(FunStash.getInstance(), 20L * FunStash.getInstance().getSettingsConfig().getHologramSeconds());
    }

    public void removeItemHolograms() {
        for (String name : new ArrayList<>(itemHolograms)) {
            Hologram hologram = DHAPI.getHologram(name);
            if (hologram != null) {
                hologram.delete();
            }
        }
        itemHolograms.clear();
    }
}