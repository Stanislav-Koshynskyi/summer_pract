package org.core.event;

import org.core.math.Vec2;

/**
 * Information about where a bullet hit and what it hit.
 */
public class HitInfo {
    public final Vec2 point;
    public final boolean hitEntity; // true = enemy/player hit, false = wall/tile hit
    public final float angle;       // direction bullet was traveling (degrees)

    public HitInfo(Vec2 point, boolean hitEntity, float angle) {
        this.point = point;
        this.hitEntity = hitEntity;
        this.angle = angle;
    }
}
