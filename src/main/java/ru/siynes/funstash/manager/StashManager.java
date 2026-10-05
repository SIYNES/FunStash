package ru.siynes.funstash.manager;

import org.bukkit.Location;
import ru.siynes.funstash.api.model.Stash;
import ru.siynes.funstash.api.model.StashLocationKey;

import java.util.Collection;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class StashManager {
    private final Map<String, Stash> stashMap = new ConcurrentHashMap<>();
    private final Map<StashLocationKey, Stash> stashLocationMap = new ConcurrentHashMap<>();

    public void addStash(Stash stash) {
        String nameKey = stash.getName().toLowerCase(Locale.ROOT);
        StashLocationKey locKey = StashLocationKey.from(stash.getLocation());

        stashMap.put(nameKey, stash);
        stashLocationMap.put(locKey, stash);
    }

    public Stash getByName(String name) {
        return stashMap.get(name.toLowerCase(Locale.ROOT));
    }

    public Stash getByLocation(Location location) {
        return stashLocationMap.get(StashLocationKey.from(location));
    }

    public Collection<Stash> getAll() {
        return stashMap.values();
    }
}
