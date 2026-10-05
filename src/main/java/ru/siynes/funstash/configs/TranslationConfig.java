package ru.siynes.funstash.configs;

import lombok.Getter;
import ru.siynes.funstash.api.config.AbstractConfig;

@Getter
public class TranslationConfig extends AbstractConfig {
    public TranslationConfig() {
        super("translation");
    }

    private String openStash;
    private String needKey;
    private String alreadyOpen;

    private String noPermissions;
    private String playerNotFound;
    private String stashNotFound;
    private String notFormat;

    @Override
    public void parse() {
        this.openStash = getConfig().getString("stash.open");
        this.needKey = getConfig().getString("stash.need-key");
        this.alreadyOpen = getConfig().getString("stash.already-open");

        this.noPermissions = getConfig().getString("commands.no-permissions");
        this.playerNotFound = getConfig().getString("commands.player-not-found");
        this.stashNotFound = getConfig().getString("commands.stash-not-found");
        this.notFormat = getConfig().getString("commands.not-format");
    }
}
