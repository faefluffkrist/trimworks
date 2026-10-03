package com.faefluffkrist.trimworks.config;

import java.util.LinkedHashSet;
import java.util.Set;

/** Independent exclusions for non-player wearers. Existing player settings still apply. */
public final class MobBonusesConfig {
    public boolean enabled = true;
    public Set<String> mainTrimBlacklist = new LinkedHashSet<>();
    public Set<String> fullSetBlacklist = new LinkedHashSet<>();
    public Set<String> materialBlacklist = new LinkedHashSet<>();

    public boolean allows(String id, int category) {
        if (!enabled) return false;
        Set<String> blacklist = category == 0 ? mainTrimBlacklist : category == 1 ? fullSetBlacklist : materialBlacklist;
        return (blacklist == null || !blacklist.contains(id))
                && (category != 1 || mainTrimBlacklist == null || !mainTrimBlacklist.contains(id));
    }
}
