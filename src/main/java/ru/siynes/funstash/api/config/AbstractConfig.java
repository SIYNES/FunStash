package ru.siynes.funstash.api.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.siynes.funstash.FunStash;

import java.io.*;

@Getter
public abstract class AbstractConfig {
    private final String name;
    private File file;
    @Getter
    private FileConfiguration config;

    public AbstractConfig(String name) {
        this.name = name;
        reload();
        load();

        if (config == null) {
            throw new IllegalStateException("ошибка парсинга " + name + ".yml");
        }

        parse();
    }

    public abstract void parse();

    private void load() {
        if (file == null) return;

        config = YamlConfiguration.loadConfiguration(file);

        InputStream stream = FunStash.getInstance().getResource(name + ".yml");
        if (stream == null) return;

        Reader reader = new InputStreamReader(stream);
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);

        config.setDefaults(defaultConfig);
    }

    private void reload() {
        this.file = new File(FunStash.getInstance().getDataFolder(), name + ".yml");

        File dir = file.getParentFile();

        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        if (!file.exists()) {
            String path = name.replace('\\', '/') + ".yml";

            try {
                InputStream resource = FunStash.getInstance().getResource(path);
                if (resource != null) {
                    FunStash.getInstance().saveResource(path, false);
                } else {
                    FunStash.getInstance().saveResource(path, false);
                }
            } catch (Exception e) {
                try {
                    file.createNewFile();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}