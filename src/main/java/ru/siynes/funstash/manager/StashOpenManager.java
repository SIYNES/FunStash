package ru.siynes.funstash.manager;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.siynes.funstash.FunStash;
import ru.siynes.funstash.api.model.LootItem;
import ru.siynes.funstash.api.model.Stash;
import ru.siynes.funstash.configs.SettingsConfig;

import java.util.*;

public class StashOpenManager {
    @Getter private Map<UUID, ItemStack> openings = new HashMap<>();
    private final SettingsConfig settingsConfig;
    private final Random random = new Random();
    private final StashHologramManager stashHologramManager;

    public StashOpenManager(SettingsConfig settingsConfig, StashHologramManager stashHologramManager) {
        this.settingsConfig = settingsConfig;
        this.stashHologramManager = stashHologramManager;
    }

    public void open(Player player, Stash stash) {
        LootItem loot = getRandomLoot(stash.getLootItems());
        if (loot == null) return;

        openings.put(player.getUniqueId(), loot.getItemStack());
        playStashAnimation(stash, loot, player);
    }

    public void playStashAnimation(Stash stash, LootItem item, Player player) {
        Block block = stash.getLocation().getBlock();
        World world = block.getWorld();

        double centerX = block.getX() + 0.5;
        double centerY = block.getY() + 0.5;
        double centerZ = block.getZ() + 0.5;
        double topY = block.getY() + 1.0;

        int waves = settingsConfig.getWaves();
        long interval = settingsConfig.getIntervalTicks();

        new BukkitRunnable() {

            int wave = 0;

            @Override
            public void run() {
                if (wave >= waves) {
                    SettingsConfig.ParticleSettings finish = settingsConfig.getFinishParticle();
                    world.spawnParticle(finish.type(), centerX, topY + 0.5, centerZ, finish.count(), 0, 0, 0, 0.3);

                    cancel();
                    stashHologramManager.createHologram(new Location(world, centerX, topY + 0.8, centerZ), item.getItemStack());
                    player.getInventory().addItem(item.getItemStack()).forEach((i, is) -> {
                        player.getWorld().dropItemNaturally(player.getLocation(), is);
                    });

                    openings.remove(player.getUniqueId());

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            stash.setActive(false);
                            stashHologramManager.show(stash);
                        }
                    }.runTaskLater(
                            FunStash.getInstance(),
                            20L * settingsConfig.getCooldownSeconds()
                    );
                    return;
                }

                SettingsConfig.ParticleSettings top = settingsConfig.getTopParticle();
                world.spawnParticle(top.type(), centerX, topY, centerZ, top.count(), 0, 0, 0, 0);

                SettingsConfig.ParticleSettings around = settingsConfig.getAroundParticle();
                world.spawnParticle(around.type(), centerX, centerY, centerZ, around.count(), 0.5, 0.5, 0.5, 0);

                wave++;
            }

        }.runTaskTimer(FunStash.getInstance(), 0L, interval);
    }

    private LootItem getRandomLoot(List<LootItem> lootList) {
        if (lootList == null || lootList.isEmpty()) return null;

        double totalWeight = lootList.stream().mapToDouble(LootItem::getChance).sum();
        if (totalWeight <= 0) return lootList.get(0);

        double randomValue = random.nextDouble() * totalWeight;
        double countWeight = 0.0;

        for (LootItem loot : lootList) {
            countWeight += loot.getChance();
            if (randomValue <= countWeight) {
                return loot;
            }
        }
        return lootList.get(lootList.size() - 1);
    }
}
