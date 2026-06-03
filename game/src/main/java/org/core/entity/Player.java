package org.core.entity;

import lombok.Getter;
import lombok.Setter;
import org.core.enums.MovementMode;
import org.core.weapon.Weapon;

public class Player extends Entity implements Damageable{

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
}

