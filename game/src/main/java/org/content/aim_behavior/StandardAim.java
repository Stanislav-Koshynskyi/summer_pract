package org.content.aim_behavior;

import org.core.behavior.AimBehavior;
import org.core.entity.Enemy;
import org.core.entity.Player;
import org.core.math.Vec2;

public class StandardAim implements AimBehavior {

    @Override
    public void updateAim(Enemy enemy, Player player, float delta) {
        float deltaAngel = new Vec2(player.getX() - enemy.getX(), player.getY() - enemy.getY()).angleDeg();
        enemy.rotateTowards(deltaAngel, delta);
    }

    @Override
    public float calculateError(Enemy enemy, Player player, float distanceToTarget) {
        return 0;
    }
}
