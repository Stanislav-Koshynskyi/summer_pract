package org.core.entity;

import lombok.Getter;
import lombok.Setter;
import org.core.collision.Blocker;
import org.core.definition.PlayerProfile;
import org.core.enums.MovementMode;
import org.core.math.Rect;
import org.core.weapon.Weapon;
import java.util.Optional;
import java.util.UUID;

public class Player extends Entity implements Damageable, Blocker {
    public static final float BASE_MOVE_SPEED = 100f;
    @Getter
    private Weapon currentWeapon;
    @Setter
    @Getter
    private Weapon defaultWeapon;
    @Setter
    @Getter
    private MovementMode movementMode;

    // в градусах
    @Setter
    @Getter
    private float facingAngle;
    @Getter
    @Setter
    private int hp = 1;
    @Getter
    @Setter
    private float speedMultiplayer = 1;
    @Getter
    @Setter
    private float ammoMultiplayer = 1;
    @Getter
    private final PlayerProfile playerProfile;
    @Getter
    @Setter
    private int bonusDamage = 0;

    public Player(float x, float y, float width, float height,
                  PlayerProfile playerProfile) {
        super(x, y, width, height);
        this.movementMode = MovementMode.WALK;
        this.facingAngle = 0f;
        this.playerProfile = playerProfile;
        this.defaultWeapon = playerProfile.getDefaultWeapon().clone();
        this.currentWeapon = playerProfile.getDefaultWeapon().clone();
        hp += playerProfile.getBonusHp();
        ammoMultiplayer = playerProfile.getAmmoMultiplier();
        speedMultiplayer = playerProfile.getSpeedMultiplier();
        bonusDamage = playerProfile.getDamageBonus();

    }

    public void aimAt(float worldX, float worldY) {
        float dx = worldX - getX();
        float dy = worldY - getY();
        if (dx * dx + dy * dy > 0.0001f) {
            facingAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        }
    }

    public void setCurrentWeapon(Weapon weapon) {
        this.currentWeapon = weapon != null ? weapon : defaultWeapon;
    }

    public void applyDamage(int i) {
        if (isAlive()) {
            hp--;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public Rect getBounds() {
        return Rect.fromCenter(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean blocksMovement() {
        return true;
    }

    @Override
    public boolean blocksVision() {
        return true;
    }

    @Override
    public boolean blocksProjectile() {
        return true;
    }

    @Override
    public boolean blocksSound() {
        return false;
    }

    @Override
    public float getSoundAttenuationFactor() {
        return 1;
    }

    public float getSpeed() {
        return BASE_MOVE_SPEED * movementMode.getSpeedMultiply() * speedMultiplayer;
    }
    public Optional<WeaponPickup> dropWeapon() {
        if (currentWeapon.equals(defaultWeapon)) return Optional.empty();
        WeaponPickup pickup = new WeaponPickup(
                getX(), getY(),
                currentWeapon.getDefinition().getId(),
                currentWeapon, getCurrentWeapon().getAmmo() == 0,
                UUID.randomUUID().toString());
        currentWeapon = defaultWeapon.clone();
        return Optional.of(pickup);
    }
    public float getMoveSoundModify(){
        return playerProfile.getMoveSoundModify();
    }
}

