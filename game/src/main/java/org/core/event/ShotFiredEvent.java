package org.core.event;

import org.core.math.Vec2;

import java.util.List;

public class ShotFiredEvent extends GameEvent {
    public float fromX, fromY;
    public String weaponId;
    List<Vec2> targets;
    public boolean isSuppressed;

    public ShotFiredEvent(float fromX, float fromY, List<Vec2> targets,
                         String weaponId, boolean isSuppressed) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.targets = targets;
        this.weaponId = weaponId;
        this.isSuppressed = isSuppressed;
    }

    @Override
    public String getEventType() {
        return "ShotFired";
    }
}


