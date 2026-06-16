package org.core.event;

import lombok.Getter;
import org.core.math.Vec2;

import java.util.List;

public class ShotFiredEvent extends GameEvent {
    public float fromX, fromY;
    public String weaponId;
    @Getter
    List<Vec2> targets;
    @Getter
    List<HitInfo> hitInfos;
    public boolean isSuppressed;

    public ShotFiredEvent(float fromX, float fromY, List<Vec2> targets,
                         String weaponId, boolean isSuppressed,
                         List<HitInfo> hitInfos) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.targets = targets;
        this.weaponId = weaponId;
        this.isSuppressed = isSuppressed;
        this.hitInfos = hitInfos;
    }

    @Override
    public String getEventType() {
        return "ShotFired";
    }
}
