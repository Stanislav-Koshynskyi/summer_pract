package org.core.controller;

import org.core.entity.Door;
import org.core.entity.Enemy;
import org.core.entity.Player;
import org.core.entity.WeaponPickup;
import org.core.enums.AIState;
import org.core.enums.DoorState;
import org.core.enums.RayCastType;
import org.core.event.DoorStateChangedEvent;
import org.core.event.WeaponPickedUpEvent;
import org.core.math.Vec2;
import org.core.raycast.RayCastResult;
import org.core.raycast.RayCastSystem;
import org.core.state.LevelState;

import java.util.Optional;
import java.util.Set;

public class InteractionSystem {
    private static final float STEALTH_KILL_RANGE = 48f;
    private static final float MAX_AIM_DEVIATION = 45f;
    private static final float INTERACT_RANGE = 48f;

    public static boolean interact(Player player, LevelState levelState, RayCastSystem rayCastSystem) {
        Vec2 playerPos = new Vec2(player.getX(), player.getY());

        Enemy targetEnemy = null;
        float bestAimDeviation = Float.MAX_VALUE;

        for (Enemy enemy : levelState.getEnemies()) {
            if (!enemy.isAlive()) continue;

            float dist = playerPos.distanceTo(enemy.getX(), enemy.getY());
            if (dist > STEALTH_KILL_RANGE) continue;

            Vec2 enemyFacingVec = Vec2.fromAngleDeg(enemy.getFacingAngle());
            Vec2 enemyToPlayer = playerPos.copy().sub(enemy.getX(), enemy.getY()).normalize();
            boolean isBehind = enemyFacingVec.dot(enemyToPlayer) < 0;
            if (!isBehind) continue;

            if (enemy.getCurrentState() == AIState.ATTACK) continue;

            Vec2 playerToEnemy = new Vec2(enemy.getX() - player.getX(), enemy.getY() - player.getY());
            float angleToEnemy = playerToEnemy.angleDeg();
            float aimDeviation = Math.abs(Vec2.angleDiff(player.getFacingAngle(), angleToEnemy));
            if (aimDeviation > MAX_AIM_DEVIATION) continue;

            if (dist > 0.001f) {
                Vec2 dir = playerToEnemy.copy().normalize();
                RayCastResult result = rayCastSystem.cast(playerPos, dir, dist, player, Set.of(player), RayCastType.VISION);
                if (result.hitTile() || (result.hitBlocker() && result.getTarget() != enemy)) {
                    continue;
                }
            }

            if (aimDeviation < bestAimDeviation) {
                targetEnemy = enemy;
                bestAimDeviation = aimDeviation;
            }
        }

        if (targetEnemy != null) {
            targetEnemy.applyDamage(targetEnemy.getHp());
            return true;
        }

        WeaponPickup nearestPickup = null;
        float minPickupDist = INTERACT_RANGE;

        for (WeaponPickup pickup : levelState.getPickups()) {
            float dist = playerPos.distanceTo(pickup.getX(), pickup.getY());
            if (dist < minPickupDist) {
                nearestPickup = pickup;
                minPickupDist = dist;
            }
        }

        if (nearestPickup != null) {
            if (nearestPickup.getWeapon().equals(player.getCurrentWeapon())){
                player.getCurrentWeapon().refillAmmo(nearestPickup.getWeapon().getAmmo());
            }
            else {
                Optional<WeaponPickup> dropped = player.dropWeapon();
                if (dropped.isPresent()) {
                    levelState.getPickups().add(dropped.get());
                }
                player.setCurrentWeapon(nearestPickup.getWeapon());
            }
            levelState.removeWeaponPickup(nearestPickup);
            levelState.addGameEvent(new WeaponPickedUpEvent(nearestPickup.getWeaponId(), player.getX(), player.getY()));
            return true;
        }

        Door nearestDoor = null;
        float minDoorDist = INTERACT_RANGE;

        for (Door door : levelState.getDoors()) {
            float dist = playerPos.distanceTo(door.getX(), door.getY());
            if (dist < minDoorDist) {
                nearestDoor = door;
                minDoorDist = dist;
            }
        }

        if (nearestDoor != null) {
            if (nearestDoor.getState() == DoorState.CLOSED) {
                nearestDoor.requestOpen();
                levelState.addGameEvent(new DoorStateChangedEvent(nearestDoor.getDoorId(), DoorState.OPENING));
            } else if (nearestDoor.getState() == DoorState.OPEN) {
                nearestDoor.requestClose();
                levelState.addGameEvent(new DoorStateChangedEvent(nearestDoor.getDoorId(), DoorState.CLOSING));
            }
            return true;
        }

        return false;
    }
}
