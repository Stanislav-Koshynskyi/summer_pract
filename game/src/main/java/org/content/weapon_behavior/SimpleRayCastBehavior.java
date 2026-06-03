package org.content.weapon_behavior;

import org.core.behavior.WeaponBehavior;
import org.core.raycast.RayCastResult;
import org.core.raycast.RayCastType;
import org.core.weapon.WeaponFireContext;
import java.util.List;

public class SimpleRayCastBehavior implements WeaponBehavior{
    @Override
    public List<RayCastResult> fire(WeaponFireContext context) {
        return List.of(context.performRaycast(RayCastType.PROJECTILE));
    }
}
