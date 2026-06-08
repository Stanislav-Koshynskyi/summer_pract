package org.core.entity;

import lombok.Getter;
import lombok.Setter;
import org.core.collision.Blocker;
import org.core.enums.MovementMode;
import org.core.math.Rect;
import org.core.weapon.Weapon;

public class Player extends Entity implements Damageable, Blocker {
    public static final float BASE_MOVE_SPEED = 150;
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

    private boolean isAlive;

    public Player(float x, float y, float width, float height,
                  Weapon defaultWeapon) {
        super(x, y, width, height);
        this.defaultWeapon = defaultWeapon;
        this.currentWeapon = defaultWeapon;
        this.movementMode = MovementMode.WALK;
        this.facingAngle = 0f;
        this.isAlive = true;
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
    public void applyDamage(int i){
        if (isAlive){
            isAlive = false;
        }
    }
    public boolean isAlive(){
        return isAlive;
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
    public float getSpeed(){
        return BASE_MOVE_SPEED * movementMode.getSpeedMultiply();
    }
}

