package ru.siynes.funstash.configs;

import lombok.Getter;
import org.bukkit.Particle;
import ru.siynes.funstash.api.config.AbstractConfig;

import java.util.Locale;

@Getter
public class SettingsConfig extends AbstractConfig {
    public SettingsConfig() {
        super("settings");
    }

    private int cooldownSeconds;
    private int hologramSeconds;
    private int waves;
    private int intervalTicks;
    private ParticleSettings topParticle;
    private ParticleSettings aroundParticle;
    private ParticleSettings finishParticle;


    @Override
    public void parse() {
        this.cooldownSeconds = getConfig().getInt("opening.cooldown-seconds");
        this.hologramSeconds = getConfig().getInt("opening.hologram-seconds");
        this.waves = getConfig().getInt("animation.waves");
        this.intervalTicks = getConfig().getInt("animation.interval-ticks");
        this.topParticle = readParticle("animation.particles.top");
        this.aroundParticle = readParticle("animation.particles.around");
        this.finishParticle = readParticle("animation.particles.finish");
    }

    private ParticleSettings readParticle(String path) {
        String type = getConfig().getString(path + ".type");
        int count = getConfig().getInt(path + ".count");

        if (type == null) {
            throw new IllegalArgumentException(
                    path + ".type частица не указана"
            );
        }

        Particle particle;

        try {
            particle = Particle.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    path + " неизвестная частица " + type
            );
        }

        return new ParticleSettings(particle, count);
    }

    public record ParticleSettings(
            Particle type,
            int count
    ) {}
}