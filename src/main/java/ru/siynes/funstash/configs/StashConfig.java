package ru.siynes.funstash.configs;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.siynes.funstash.FunStash;
import ru.siynes.funstash.api.config.AbstractConfig;
import ru.siynes.funstash.api.model.HologramSettings;
import ru.siynes.funstash.api.model.LootItem;
import ru.siynes.funstash.api.model.Stash;
import ru.siynes.funstash.utils.ColorUtil;

import java.util.*;

@Slf4j
@Getter
public class StashConfig extends AbstractConfig {

    private Map<String, Stash> stashes;

    public StashConfig() {
        super("stash");
    }

    @Override
    public void parse() {
        if (stashes == null) {
            stashes = new LinkedHashMap<>();
        } else {
            stashes.clear();
        }

        ConfigurationSection section = getConfig().getConfigurationSection("stashes");

        if (section == null) throw new IllegalArgumentException(
                "stash.yml - секция stashes не найдена"
        );

        for (String id : section.getKeys(false)) {
            String path = "stashes." + id;
            Stash stash = new Stash();

            stash.setName(id);
            stash.setLocation(loadLocation(path + ".location"));
            stash.setActive(false);
            stash.setStashKey(loadItem(path + ".key"));
            stash.setLootItems(loadLoot(path + ".loot"));
            stash.setHologram(loadHologram(path + ".hologram"));

            stashes.put(id.toLowerCase(), stash);
        }
    }

    private List<LootItem> loadLoot(String path) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section == null) throw new IllegalArgumentException(path + " - секция не указана");

        List<LootItem> items = new ArrayList<>();
        for (String id : section.getKeys(false)) {
            String itemPath = path + "." + id;
            ItemStack item = loadItem(itemPath);

            int amount = getConfig().getInt(itemPath + ".amount");
            double chance = getConfig().getDouble(itemPath + ".chance");
            if (chance < 0.0 || chance > 1.0) {
                throw new IllegalArgumentException(itemPath + ".chance - значение должно быть от 0 до 1");
            }

            LootItem lootItem = new LootItem(item, chance, amount);
            items.add(lootItem);
        }

        return items;
    }

    private HologramSettings loadHologram(String path) {
        ConfigurationSection section = getConfig().getConfigurationSection(path);

        if (section == null) {
            return new HologramSettings(true, 0.5, 1.8, 0.5, List.of());
        }

        List<String> lines = section.isList("lines")
                ? List.copyOf(section.getStringList("lines"))
                : List.of();

        return new HologramSettings(section.getBoolean("enabled", true), section.getDouble("offset.x", 0.5), section.getDouble("offset.y", 1.8), section.getDouble("offset.z", 0.5), lines);
    }


    private ItemStack loadItem(String path) {
        Material material = Material.valueOf(getConfig().getString(path + ".material"));

        int amount = getConfig().getInt(path + ".amount", 1);
        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        String name = ColorUtil.translate(getConfig().getString(path + ".name"));
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        for (String line : getConfig().getStringList(path + ".lore")) {
            lore.add(ColorUtil.translate(line));
        }
        meta.setLore(lore);

        Set<ItemFlag> itemFlags = loadItemFlag(path);
        for (ItemFlag flag : itemFlags) {
            meta.addItemFlags(flag);
        }

        Map<Enchantment, Integer> enchantment = loadEnchantment(path);
        for (Map.Entry<Enchantment, Integer> entry : enchantment.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        Map<Attribute, AttributeModifier> attributes = loadAttributes(path);
        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entrySet()) {
            meta.addAttributeModifier(entry.getKey(), entry.getValue());
        }

        Map<NamespacedKey, String> pdc = loadPDC(path);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (Map.Entry<NamespacedKey, String> entry : pdc.entrySet()) {
            container.set(entry.getKey(), PersistentDataType.STRING, entry.getValue());
        }

        item.setItemMeta(meta);

        return item;
    }


    private Location loadLocation(String path) {
        String worldName = getConfig().getString(path + ".world");
        World world = FunStash.getInstance().getServer().getWorld(worldName);

        double x = getConfig().getDouble(path + ".x");
        double y = getConfig().getDouble(path + ".y");
        double z = getConfig().getDouble(path + ".z");

        return new Location(world, x, y, z);
    }

    private Set<ItemFlag> loadItemFlag(String path) {
        Set<ItemFlag> itemFlags = new HashSet<>();
        List<String> flags = getConfig().getStringList(path + ".itemFlags");
        for (String flagName : flags) {
            ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
            itemFlags.add(flag);
        }
        return itemFlags;
    }

    private Map<Enchantment, Integer> loadEnchantment(String path) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection(path + ".enchantments");
        if (section != null) {
            for (String enchantment : section.getKeys(false)) {
                int lvl = section.getInt(enchantment);
                enchantments.put(Enchantment.getByName(enchantment), lvl);
            }
        }
        return enchantments;
    }

    private Map<Attribute, AttributeModifier> loadAttributes(String path) {
        Map<Attribute, AttributeModifier> attributes = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection(path + ".attributes");
        if (section != null) {
            for (String atrributeKey : section.getKeys(false)) {
                Attribute attribute = Attribute.valueOf(atrributeKey);
                ConfigurationSection sectionAttriibute = section.getConfigurationSection(atrributeKey);

                String name = sectionAttriibute.getString("name");
                double amount = sectionAttriibute.getDouble("amount");
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(sectionAttriibute.getString("operation"));
                EquipmentSlot slot = EquipmentSlot.valueOf(sectionAttriibute.getString("slot"));

                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), name, amount, operation, slot);

                attributes.put(attribute, modifier);
            }
        }
        return attributes;
    }

    private Map<NamespacedKey, String> loadPDC(String path) {
        Map<NamespacedKey, String> pdc = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection(path + ".pdc");
        if (section != null) {
            for (String pdcKey : section.getKeys(false)) {
                NamespacedKey key = new NamespacedKey(FunStash.getInstance(), pdcKey.toLowerCase());
                String value = section.getString(pdcKey);
                pdc.put(key, value);
            }
        }
        return pdc;
    }
}