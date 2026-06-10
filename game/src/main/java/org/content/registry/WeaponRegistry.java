package org.content.registry;

import org.core.weapon.WeaponDefinition;

import java.util.HashMap;
import java.util.Map;

public class WeaponRegistry {
    private final Map<String, WeaponDefinition> weapons = new HashMap<>();

    public void register(WeaponDefinition weapon){
        weapons.put(weapon.getId(), weapon);
    }

    public WeaponDefinition get(String id){
        WeaponDefinition weapon = weapons.get(id);
        if (weapon == null) {
            throw new IllegalArgumentException("Unknown weapon type id: " + id);
        }
        return weapon;
    }
}
