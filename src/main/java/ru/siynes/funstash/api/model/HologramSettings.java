package ru.siynes.funstash.api.model;

import java.util.List;

public record HologramSettings(
        boolean enabled,
        double offsetX,
        double offsetY,
        double offsetZ,
        List<String> lines
) {}