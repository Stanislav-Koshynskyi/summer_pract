package org.core.weapon;

import lombok.Getter;
import org.core.behavior.WeaponBehavior;
import org.core.enums.WeaponType;
@Getter
public class WeaponDefinition {
    private final String id;
    private final WeaponType weaponType;
    private final int damage;
    private final float range;
    private final float cooldown;
    private final int maxAmmo;          // -1 = нескінченно
    private final boolean isSuppressed;
    private final boolean isAutomatic;
    private final WeaponBehavior behavior;
    @Getter
    private final float knockbackForce;

    public WeaponDefinition(String id, WeaponType weaponType, int damage, float range,
                            float cooldown, int maxAmmo, boolean isSuppressed,
                            boolean isAutomatic, WeaponBehavior behavior, float knockbackForce) {
        this.id = id;
        this.weaponType = weaponType;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.maxAmmo = maxAmmo;
        this.isSuppressed = isSuppressed;
        this.isAutomatic = isAutomatic;
        this.behavior = behavior;
        this.knockbackForce = knockbackForce;
    }

}