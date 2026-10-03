package com.faefluffkrist.trimworks.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfiguredEffect {
    public String id;
    public Map<String, Integer> levels = new LinkedHashMap<>();

    public ConfiguredEffect() {}

    public ConfiguredEffect(String id, int one, int two, int three, int four) {
        this.id = id;
        levels.put("1", one);
        levels.put("2", two);
        levels.put("3", three);
        levels.put("4", four);
    }
}
