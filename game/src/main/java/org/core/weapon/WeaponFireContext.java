package org.core.weapon;

import lombok.Getter;
import org.core.entity.Entity;
import org.core.math.Vec2;
import org.core.raycast.RayCastResult;
import org.core.raycast.RayCastSystem;
import org.core.raycast.RayCastType;

import java.util.Set;
@Getter
public class WeaponFireContext {
    private final RayCastSystem raycastSystem;
    private final Entity shooter;
    private final Vec2 origin;
    private final Vec2 direction;
    private final float maxDistance;
    private final Set<Entity> ignoredEntities;
    private final int damage;

    public WeaponFireContext(RayCastSystem raycastSystem,
                             Entity shooter,
                             Vec2 origin,
                             Vec2 direction,
                             float maxDistance,
                             Set<Entity> ignoredEntities,
                             int damage) {
        this.raycastSystem = raycastSystem;
        this.shooter = shooter;
        this.origin = origin;
        this.direction = direction;
        this.maxDistance = maxDistance;
        this.ignoredEntities = ignoredEntities;
        this.damage = damage;
    }



   // стандартний рейкаст
    public RayCastResult performRaycast(RayCastType type) {
        return raycastSystem.cast(origin, direction, maxDistance, shooter, ignoredEntities, type);
    }

    // інший рейкаст
    public RayCastResult performRaycast(Vec2 customDirection, RayCastType type) {
        return raycastSystem.cast(origin, customDirection, maxDistance, shooter, ignoredEntities, type);
    }
}