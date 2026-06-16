package org.content.weapon_behavior;

/**
 * Defines the visual properties of a weapon's bullet projectile.
 * Configured per-weapon in WeaponRegistry.
 */
public class BulletVisualProfile {
    private final float bulletSpeed;
    private final float bulletSize;

    public BulletVisualProfile(float bulletSpeed, float bulletSize) {
        this.bulletSpeed = bulletSpeed;
        this.bulletSize = bulletSize;
    }

    public float getBulletSpeed() {
        return bulletSpeed;
    }

    public float getBulletSize() {
        return bulletSize;
    }

    /** Default fallback profile for unknown weapons */
    public static final BulletVisualProfile DEFAULT = new BulletVisualProfile(3000f, 15f);
}
