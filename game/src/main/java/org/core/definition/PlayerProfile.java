package org.core.definition;

import lombok.Getter;
import org.core.weapon.Weapon;

import java.util.List;

@Getter
public class PlayerProfile {
    private final String id;
    private final Weapon startingWeapon;
    private final Weapon defaultWeapon;
    private final int bonusHp;
    private final float speedMultiplier;
    private final float ammoMultiplier;
    private final int damageBonus;
    private final float moveSoundModify;

    public PlayerProfile(String id, Weapon startingWeapon, Weapon defaultWeapon, int bonusHp,
                         float speedMultiplier, float ammoMultiplier, float interactRadiusMultiplier,
                         int damageBonus, float moveSoundModify) {
        this.id = id;
        this.startingWeapon = startingWeapon;
        this.defaultWeapon = defaultWeapon;
        this.speedMultiplier = speedMultiplier;
        this.ammoMultiplier = ammoMultiplier;
        this.bonusHp = bonusHp;
        this.damageBonus = damageBonus;
        this.moveSoundModify = moveSoundModify;
    }
}