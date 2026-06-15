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
    private final String nameUA;
    private final String nameEN;
    private final String descriptionUA;
    private final String descriptionEN;


    public PlayerProfile(String id, Weapon startingWeapon, Weapon defaultWeapon, int bonusHp,
                         float speedMultiplier, float ammoMultiplier, float interactRadiusMultiplier,
                         int damageBonus, float moveSoundModify,
                         String nameUA, String nameEN, String descriptionUA, String descriptionEN) {
        this.id = id;
        this.startingWeapon = startingWeapon;
        this.defaultWeapon = defaultWeapon;
        this.speedMultiplier = speedMultiplier;
        this.ammoMultiplier = ammoMultiplier;
        this.bonusHp = bonusHp;
        this.damageBonus = damageBonus;
        this.moveSoundModify = moveSoundModify;
        this.nameEN = nameEN;
        this.nameUA = nameUA;
        this.descriptionEN = descriptionEN;
        this.descriptionUA = descriptionUA;
    }
    public String getCurrentName(boolean isUa){
        return isUa ? nameUA : nameEN;
    }
    public String getCurrentDescription(boolean isUa){
        return  isUa ? descriptionUA : descriptionEN;
    }
}