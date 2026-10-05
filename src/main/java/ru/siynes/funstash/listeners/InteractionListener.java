package ru.siynes.funstash.listeners;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.siynes.funstash.FunStash;
import ru.siynes.funstash.api.model.Stash;
import ru.siynes.funstash.configs.StashConfig;
import ru.siynes.funstash.configs.TranslationConfig;
import ru.siynes.funstash.manager.StashManager;
import ru.siynes.funstash.utils.ColorUtil;

import java.util.Arrays;

public class InteractionListener implements Listener {
    private final StashManager stashManager;
    private final TranslationConfig config;
    private final StashConfig stashConfig;

    public InteractionListener() {
        stashManager = FunStash.getInstance().getStashManager();
        config = FunStash.getInstance().getTranslationConfig();
        stashConfig = FunStash.getInstance().getStashConfig();
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        Location blockLocation = block.getLocation();
        Stash stash = stashManager.getByLocation(blockLocation);
        Player player = e.getPlayer();
        if (stash != null) {
            e.setCancelled(true);
            if (!(stash.isActive())) {
                ItemStack item = e.getItem();
                if (item != null && isStashKey(item, stash)) {
                    item.setAmount(item.getAmount() - 1);
                    stash.setActive(true);
                    FunStash.getInstance().getHologramManager().hide(stash);
                    player.sendMessage(ColorUtil.translate(config.getOpenStash()));
                    FunStash.getInstance().getStashOpenManager().open(player, stash);
                } else {
                    player.sendMessage(ColorUtil.translate(config.getNeedKey()));
                }
            } else {
                player.sendMessage(ColorUtil.translate(config.getAlreadyOpen()));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();

        for (Stash stash : stashConfig.getStashes().values()) {
            if (isStashKey(item, stash)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private boolean isStashKey(ItemStack item, Stash stash) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        ItemMeta stashKeyMeta = stash.getStashKey().getItemMeta();
        if (stashKeyMeta == null) return false;

        PersistentDataContainer stashPdc = stashKeyMeta.getPersistentDataContainer();
        PersistentDataContainer itemPdc = meta.getPersistentDataContainer();

        for (NamespacedKey key : stashPdc.getKeys()) {
            if (itemPdc.has(key, PersistentDataType.STRING)) {
                String expected = stashPdc.get(key, PersistentDataType.STRING);
                String actual = itemPdc.get(key, PersistentDataType.STRING);

                if (expected != null && expected.equals(actual)) {
                    return true;
                }
            }
        }

        return false;
    }
}

