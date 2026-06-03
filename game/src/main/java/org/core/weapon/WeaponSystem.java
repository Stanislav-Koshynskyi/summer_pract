package org.core.weapon;

import org.core.behavior.WeaponBehavior;
import org.core.enums.WeaponType;
import org.core.event.GameEvent;
import org.core.event.MeleeAttackEvent;
import org.core.event.ShotFiredEvent;
import org.core.math.Vec2;
import org.core.raycast.RayCastResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponSystem {
    public List<GameEvent> useWeapon(WeaponFireContext context, Weapon weapon) {
        WeaponBehavior behavior = weapon.getBehavior();
        WeaponDefinition def = weapon.getDefinition();
        List<RayCastResult> hits = behavior.fire(context);
        List<GameEvent> events = new ArrayList<>();

        for (RayCastResult hit : hits) {
            hit.tryApplyDamage(context.getDamage());
        }

        WeaponType type = def.getWeaponType();
        List<Vec2> hitPoints = hits.stream().map(RayCastResult::getPoint).collect(Collectors.toList());

        switch (type) {
            case HITSCAN -> {
                events.add(new ShotFiredEvent(
                        context.getOrigin().x,
                        context.getOrigin().y,
                        hitPoints,
                        def.getId(),
                        def.isSuppressed()
                ));
            }
            case MELEE -> {
                events.add(new MeleeAttackEvent(
                        context.getOrigin().x,
                        context.getOrigin().y,
                        context.getDirection().angleDeg(),
                        def.getId(),
                        hitPoints
                ));
            }
        }

        weapon.onFire();

        return events;
    }
}
