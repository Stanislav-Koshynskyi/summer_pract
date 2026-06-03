package org.core.raycast;


import org.core.collision.Blocker;
import org.core.enums.RayCastType;
import org.core.enums.TileType;
import org.core.geometry.WorldGeometry;
import org.core.math.Rect;
import org.core.math.Vec2;
import org.core.entity.Entity;

import java.util.List;
import java.util.Set;

public class RayCastSystem {

    private final WorldGeometry geometry;
    private final List<Blocker> blockers;

    public RayCastSystem(WorldGeometry geometry, List<Blocker> blockers) {
        this.geometry = geometry;
        this.blockers = blockers;
    }

    public RayCastResult cast(Vec2 from, Vec2 direction, float maxDistance,
                              Entity shooter, Set<Entity> ignoredEntities, RayCastType mode) {
        float dirLen = direction.length();
        if (dirLen < 0.0001f) {
            return new RayCastResult(from.copy(), 0f, null, null);
        }
        Vec2 dir = direction.copy().scale(1f / dirLen); // нормалізуємо, треба для коректної роботи рейкаста бо метод в rect працює з нормалізованими векторами
        float tileSize = geometry.getTileSize();

        int tileX = (int) Math.floor(from.x / tileSize);
        int tileY = (int) Math.floor(from.y / tileSize);
        int stepX = (dir.x > 0) ? 1 : -1;
        int stepY = (dir.y > 0) ? 1 : -1;
        float tDeltaX = (dir.x != 0) ? Math.abs(tileSize / dir.x) : Float.MAX_VALUE;
        float tDeltaY = (dir.y != 0) ? Math.abs(tileSize / dir.y) : Float.MAX_VALUE;
        float tMaxX, tMaxY;
        if (dir.x > 0) {
            tMaxX = ((tileX + 1) * tileSize - from.x) / dir.x;
        } else if (dir.x < 0) {
            tMaxX = (tileX * tileSize - from.x) / dir.x;
        } else {
            tMaxX = Float.MAX_VALUE;
        }
        if (dir.y > 0) {
            tMaxY = ((tileY + 1) * tileSize - from.y) / dir.y;
        } else if (dir.y < 0) {
            tMaxY = (tileY * tileSize - from.y) / dir.y;
        } else {
            tMaxY = Float.MAX_VALUE;
        }

        float t = 0f;
        TileType hitTile = null;
        float tileHitDist = maxDistance;

        while (t < maxDistance) {
            TileType currentTile = geometry.getTile(tileX, tileY);
            boolean blocked = (mode == RayCastType.VISION)
                    ? currentTile.blocksVision()
                    : currentTile.blocksProjectile();
            if (blocked) {
                hitTile = currentTile;
                tileHitDist = t;
                break;
            }
            if (tMaxX < tMaxY) {
                t = tMaxX;
                if (t > maxDistance) break;
                tileX += stepX;
                tMaxX += tDeltaX;
            } else {
                t = tMaxY;
                if (t > maxDistance) break;
                tileY += stepY;
                tMaxY += tDeltaY;
            }
        }

        Blocker closestTarget = null;
        float closestDist = maxDistance;
        Vec2 closestPoint = null;

        for (Blocker blocker : blockers) {
            if (blocker == shooter) continue;// самі себе ігноруємо
            // це треба для ввімкнення/вимкнення френдлі фаєра тощо
            if (ignoredEntities != null && ignoredEntities.contains(blocker)) continue;

            boolean blocked = (mode == RayCastType.VISION)
                    ? blocker.blocksVision()
                    : blocker.blocksProjectile();
            if (!blocked) continue;

            Rect bounds = blocker.getBounds();
            float hitDist = bounds.rayCast(from, dir, maxDistance);
            if (hitDist >= 0 && hitDist < closestDist) {
                closestDist = hitDist;
                closestTarget = blocker;
                closestPoint = from.copy().add(dir.copy().scale(hitDist));
            }
        }

        if (closestTarget != null && closestDist <= tileHitDist) {
            return new RayCastResult(closestPoint, closestDist, closestTarget, null);
        } else if (hitTile != null) {
            Vec2 tilePoint = from.copy().add(dir.copy().scale(tileHitDist));
            return new RayCastResult(tilePoint, tileHitDist, null, hitTile);
        } else {
            Vec2 endPoint = from.copy().add(dir.copy().scale(maxDistance));
            return new RayCastResult(endPoint, maxDistance, null, null);
        }
    }
}