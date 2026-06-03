package org.core.weapon;

import lombok.Getter;
import org.core.behavior.WeaponBehavior;

public class Weapon {
    @Getter
    private final WeaponDefinition definition;
    @Getter
    private int ammo;
    private float cooldownTimer;

    public Weapon(WeaponDefinition definition) {
        this.definition = definition;
        this.ammo = definition.getMaxAmmo();
        this.cooldownTimer = 0f;
    }

    public boolean canFire() {
        return cooldownTimer <= 0f && (ammo > 0 || ammo == -1);
    }


    public void updateCooldown(float delta) {
        if (cooldownTimer > 0f) {
            cooldownTimer -= delta;
        }
    }

    public void onFire() {
        cooldownTimer = definition.getCooldown();
        if (ammo > 0) ammo--;
    }

    public WeaponBehavior getBehavior() {
        return definition.getBehavior();
    }

    public boolean isEmpty() {
        return ammo == 0;
    }
}