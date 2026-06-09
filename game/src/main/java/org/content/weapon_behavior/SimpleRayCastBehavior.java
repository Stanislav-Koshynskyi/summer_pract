package org.content.weapon_behavior;

import org.core.behavior.WeaponBehavior;
import org.core.math.Vec2;
import org.core.raycast.RayCastResult;
import org.core.enums.RayCastType;
import org.core.weapon.WeaponFireContext;
import java.util.List;

public class SimpleRayCastBehavior implements WeaponBehavior{
    @Override
    public List<RayCastResult> fire(WeaponFireContext context) {
        Vec2 newDirection = context.applySpread(context.getDirection(), context.getSpreadAngle());
        WeaponFireContext newContext = new WeaponFireContext(context, newDirection);
        return List.of(newContext.performRaycast(RayCastType.PROJECTILE));
    }
}
