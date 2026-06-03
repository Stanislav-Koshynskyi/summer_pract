package org.core.behavior;

import org.core.raycast.RayCastResult;
import org.core.weapon.WeaponFireContext;

import java.util.List;

public interface WeaponBehavior {
    List<RayCastResult> fire(WeaponFireContext context);
}
