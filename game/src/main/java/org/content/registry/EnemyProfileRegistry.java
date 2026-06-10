package org.content.registry;

import org.core.definition.EnemyProfile;

import java.util.HashMap;
import java.util.Map;

public class EnemyProfileRegistry {
    private final Map<String, EnemyProfile> profiles = new HashMap<>();

    public void register(EnemyProfile profile) {
        profiles.put(profile.getEnemyTypeId(), profile);
    }

    public EnemyProfile get(String id) {
        EnemyProfile profile = profiles.get(id);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown enemy type id: " + id);
        }
        return profile;
    }
}
