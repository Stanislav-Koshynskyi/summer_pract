package org.core.raycast;

import org.core.collision.Blocker;
import org.core.entity.Damageable;
import org.core.entity.Entity;
import org.core.enums.TileType;
import org.core.math.Vec2;

public class RayCastResult {
    private final Vec2 point;
    private final float distance;
    private final Blocker target;
    private final TileType tile;

    public RayCastResult(Vec2 point, float distance, Blocker target, TileType tile) {
        this.point = point;
        this.distance = distance;
        this.target = target;
        this.tile = tile;
    }

    public Vec2 getPoint() {
        return point;
    }

    public float getDistance() {
        return distance;
    }

    public Blocker getTarget() {
        return target;
    }

    public TileType getTile() {
        return tile;
    }

    public boolean hitBlocker() {
        return target != null;
    }

    public boolean hitTile() {
        return tile != null;
    }

    public boolean tryApplyDamage(int damage) {
        if (target instanceof Damageable d) {
            d.applyDamage(damage);
            return true;
        }
        return false;
    }
}