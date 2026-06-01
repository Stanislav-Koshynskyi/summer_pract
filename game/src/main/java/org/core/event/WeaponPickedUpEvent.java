package org.core.event;

public class WeaponPickedUpEvent extends GameEvent {
    public String weaponId;
    public float x, y;

    public WeaponPickedUpEvent(String weaponId, float x, float y) {
        this.weaponId = weaponId;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getEventType() {
        return "WeaponPickedUp";
    }
}


