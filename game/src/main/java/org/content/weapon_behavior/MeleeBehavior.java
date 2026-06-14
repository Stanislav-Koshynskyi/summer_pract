package org.content.weapon_behavior;

import org.core.behavior.WeaponBehavior;
import org.core.enums.RayCastType;
import org.core.math.Vec2;
import org.core.raycast.RayCastResult;
import org.core.weapon.WeaponFireContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeleeBehavior implements WeaponBehavior {
    private static final float ATTACK_ANGLE = 60f;
    private static final int RAY_COUNT = 5;

    @Override
    public List<RayCastResult> fire(WeaponFireContext context) {
        List<RayCastResult> results = new ArrayList<>();
        Vec2 origin = context.getOrigin();
        Vec2 dir = context.getDirection();
        float range = context.getMaxDistance();

        float step = ATTACK_ANGLE / (RAY_COUNT - 1);
        float baseAngle = dir.angleDeg();
        float startAngle = baseAngle - ATTACK_ANGLE / 2f;

        for (int i = 0; i < RAY_COUNT; i++) {
            float angle = startAngle + i * step;
            Vec2 rayDir = Vec2.fromAngleDeg(angle);
            RayCastResult hit = context.performRaycast(rayDir, RayCastType.PROJECTILE);
            results.add(hit);
        }
        Set<Object> hitTargets = new HashSet<>();
        List<RayCastResult> uniqueResults = new ArrayList<>();
        for (RayCastResult hit : results) {
            if (hit.hitBlocker()) {
                Object target = hit.getTarget();
                if (!hitTargets.contains(target)) {
                    hitTargets.add(target);
                    uniqueResults.add(hit);
                }
            }
        }

        return uniqueResults;
    }
}
