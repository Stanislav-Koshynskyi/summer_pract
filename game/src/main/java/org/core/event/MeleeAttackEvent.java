package org.core.event;

import org.core.math.Vec2;

import java.util.List;

public class MeleeAttackEvent extends GameEvent {
    public final float attackerX, attackerY;
    public final float directionDeg;
    public final String weaponId;
    public final List<Vec2> hitPoints;

    public MeleeAttackEvent(float attackerX, float attackerY, float directionDeg,
                            String weaponId, List<Vec2> hitPoints) {
        this.attackerX = attackerX;
        this.attackerY = attackerY;
        this.directionDeg = directionDeg;
        this.weaponId = weaponId;
        this.hitPoints = hitPoints;
    }

    @Override
    public String getEventType() {
        return "MeleeAttack";
    }
}
