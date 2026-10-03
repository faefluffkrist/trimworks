package com.faefluffkrist.trimworks.config;

import java.util.ArrayList;
import java.util.List;

public final class TrimDefinition {
    public boolean enabled = true;
    public List<ConfiguredEffect> effects = new ArrayList<>();

    public TrimDefinition() {}

    public TrimDefinition(ConfiguredEffect effect) {
        effects.add(effect);
    }
}
