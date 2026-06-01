package org.core.entity;

import org.core.enums.MovementMode;import org.core.weapon.Weapon;public class Player extends Entity{
    private Weapon currentWeapon;
    private Weapon defaultWeapon;
    private MovementMode movementMode;
    public Player(float x, float y, float width, float height,
                  Weapon defaultWeapon) {
        super(x, y, width, height);
        this.defaultWeapon = defaultWeapon;
        this.currentWeapon = defaultWeapon;
        this.movementMode = MovementMode.WALK;
    }
}
