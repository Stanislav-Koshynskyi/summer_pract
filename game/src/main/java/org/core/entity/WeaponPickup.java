package org.core.entity;

import lombok.Getter;
import org.core.weapon.Weapon;

@Getter
public class WeaponPickup extends Entity {
    private final String pickupId;
    private final String weaponId;
    private final Weapon weapon;
    private final boolean isEmpty;
    private final static float DEFAULT_SIZE = 20f;

    public WeaponPickup(float x, float y,
                       String weaponId, Weapon weapon, boolean isEmpty, String pickupId) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
        this.weaponId = weaponId;
        this.weapon = weapon;
        this.isEmpty = isEmpty;
        this.pickupId = pickupId;
    }

    @Override
    public String toString() {
        return String.format("WeaponPickup(weapon id=%s, pickup id=%s isEmpty=%s, pos=(%.0f,%.0f))",
                           weaponId, pickupId, isEmpty, getX(), getY());
    }
    public boolean canPick(){
        return !isEmpty;
    }
}


