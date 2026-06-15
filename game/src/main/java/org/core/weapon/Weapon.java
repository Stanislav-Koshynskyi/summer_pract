package org.core.weapon;

import lombok.Getter;
import org.core.behavior.WeaponBehavior;
import org.core.definition.WeaponDefinition;

public class Weapon {
    @Getter
    private final WeaponDefinition definition;
    @Getter
    private int ammo;
    private float cooldownTimer;
    private int maxAmmo;
    // встановлюємо true якщо в ворога, щоб не тратились кулі
    private boolean infinityAmmo;


    public Weapon(WeaponDefinition definition) {
        this.definition = definition;
        this.ammo = definition.getMaxAmmo();
        this.cooldownTimer = 0f;
        infinityAmmo = false;
        this.maxAmmo = definition.getMaxAmmo();
    }

    public Weapon(WeaponDefinition definition, boolean infinityAmmo) {
        this(definition);
        this.infinityAmmo = infinityAmmo;
    }

    public boolean canFire() {
        return cooldownTimer <= 0f && (ammo > 0 || ammo == -1 || infinityAmmo);
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

    public Weapon clone() {
        Weapon weapon = new Weapon(this.getDefinition());
        weapon.ammo = this.ammo;
        return weapon;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Weapon w){
            return getDefinition().equals(w.getDefinition());
        }
        return false;
    }
    public void refillAmmo(int amount) {
        if (!infinityAmmo && amount > 0) {
            ammo = Math.min(ammo + amount, maxAmmo);
        }
    }
    public void applyAmmoMultiplier(float multiplier) {
        if (ammo == -1) return;
        if (multiplier == 1.0f) return;

        int newMax = Math.round(definition.getMaxAmmo() * multiplier);
        maxAmmo = newMax;
        if (newMax < 1) newMax = 0;

        float ratio = (definition.getMaxAmmo() > 0) ? (float) ammo / definition.getMaxAmmo() : 1f;
        ammo = Math.round(newMax * ratio);
    }
}