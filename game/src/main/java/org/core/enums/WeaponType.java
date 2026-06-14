package org.core.enums;

import lombok.Getter;

public enum WeaponType {
    MELEE(80), HITSCAN(450);
    @Getter
    private final float soundRadius;
    WeaponType(float soundRadius){
        this.soundRadius = soundRadius;
    }
}
