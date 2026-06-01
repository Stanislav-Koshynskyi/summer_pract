package org.core.event;

public class ShotFiredEvent extends GameEvent {
    public float fromX, fromY;
    public float hitPointX, hitPointY;
    public String weaponId;
    public boolean isSuppressed;

    public ShotFiredEvent(float fromX, float fromY, float hitPointX, float hitPointY,
                         String weaponId, boolean isSuppressed) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.hitPointX = hitPointX;
        this.hitPointY = hitPointY;
        this.weaponId = weaponId;
        this.isSuppressed = isSuppressed;
    }

    @Override
    public String getEventType() {
        return "ShotFired";
    }
}


