package org.core.behavior;

import org.core.entity.Enemy;
import org.core.entity.Player;

public interface AimBehavior {
    void updateAim(Enemy enemy, Player player, float delta);
    float calculateError(Enemy enemy, Player player, float distanceToTarget);
}