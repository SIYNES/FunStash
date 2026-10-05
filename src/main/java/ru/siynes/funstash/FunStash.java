package ru.siynes.funstash;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.siynes.funstash.commands.FunStashCommand;
import ru.siynes.funstash.configs.SettingsConfig;
import ru.siynes.funstash.configs.StashConfig;
import ru.siynes.funstash.configs.TranslationConfig;
import ru.siynes.funstash.listeners.InteractionListener;
import ru.siynes.funstash.manager.StashHologramManager;
import ru.siynes.funstash.manager.StashManager;
import ru.siynes.funstash.manager.StashOpenManager;

import java.util.UUID;

@Getter
public final class FunStash extends JavaPlugin {
    @Getter private static FunStash instance;
    private StashManager stashManager;
    private SettingsConfig settingsConfig;
    private TranslationConfig translationConfig;
    private StashConfig stashConfig;
    private StashOpenManager stashOpenManager;
    private StashHologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        settingsConfig = new SettingsConfig();
        translationConfig = new TranslationConfig();
        stashConfig = new StashConfig();

        stashManager = new StashManager();
        stashConfig.getStashes().values().forEach(stashManager::addStash);
        hologramManager = new StashHologramManager();
        hologramManager.showAll(stashManager.getAll());
        stashOpenManager = new StashOpenManager(settingsConfig, hologramManager);

        getCommand("funstash").setExecutor(new FunStashCommand());
        getServer().getPluginManager()
                .registerEvents(new InteractionListener(), this);
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.removeAll(stashManager.getAll());
        }

        for (UUID uuid : stashOpenManager.getOpenings().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().addItem(stashOpenManager.getOpenings().get(uuid)).forEach((i, is) -> {
                player.getWorld().dropItemNaturally(player.getLocation(), is);
            });
            stashOpenManager.getOpenings().remove(uuid);
        }
    }
}