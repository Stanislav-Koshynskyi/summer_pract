package org.core.ai;

import org.core.behavior.AimBehavior;
import org.core.controller.GameController;
import org.core.entity.Door;
import org.core.entity.Enemy;
import org.core.entity.Entity;
import org.core.entity.Player;
import org.core.enums.AIState;
import org.core.enums.AimBehaviorType;
import org.core.event.GameEvent;
import org.core.geometry.WorldGeometry;
import org.core.math.Vec2;
import org.core.raycast.RayCastSystem;
import org.core.weapon.Weapon;
import org.core.weapon.WeaponFireContext;
import org.core.weapon.WeaponSystem;

import java.util.*;

public class EnemyAI {

    // Як часто перебудовувати шлях у ATTACK (секунди)
    private static final float ATTACK_REPEAT_INTERVAL = 0.1f;
    // Мінімальна зміна позиції гравця для перебудови шляху (тайлів)
    private static final float REPAT_DISTANCE_THRESHOLD = 2f;

    private static final float PIXEL_TOLERANCE = 5;
    private static final float DEGREE_TOLERANCE = 10;
    private static final float INTERACTION_RADIUS = 32;

    // коли останній раз міняли шлях, треба щоб не рахувати шлях дуже часто
    private final Map<Enemy, Float> lastPathUpdate = new HashMap<>();

    private final VisionSystem visionSystem;
    private final List<Enemy> enemies;
    private final Player player;
    private final PathFinder pathfinder;
    private final WeaponSystem weaponSystem;
    private final RayCastSystem rayCastSystem;
    private final Map<AimBehaviorType, AimBehavior> aimBehaviors;
    private final List<Door> doors;
    private final WorldGeometry worldGeometry;

    public EnemyAI(VisionSystem visionSystem, List<Enemy> enemies, Player player, PathFinder pathfinder,
                   WeaponSystem weaponSystem, RayCastSystem rayCastSystem, Map<AimBehaviorType,
                    AimBehavior> aimBehaviors, List<Door> doors, WorldGeometry worldGeometry) {
        this.visionSystem = visionSystem;
        this.enemies = enemies;
        this.player = player;
        this.pathfinder = pathfinder;
        this.weaponSystem = weaponSystem;
        this.rayCastSystem = rayCastSystem;
        this.aimBehaviors = aimBehaviors;
        this.doors = doors;
        this.worldGeometry = worldGeometry;
    }

    public List<GameEvent> update(float delta) {
        Set<Entity> ignoredEntities = new HashSet<>(enemies);
        List<GameEvent> events = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            float vx = enemy.getVelocityX();
            float vy = enemy.getVelocityY();
            // якщо є кнокбек то ворог не може адекватно реагувати
            if (Math.abs(vx) > 0.1f || Math.abs(vy) > 0.1f){
                continue;
            }

            updateTimers(enemy, delta);

            boolean seesPlayer = visionSystem.canEnemySeePlayer(enemy, player, ignoredEntities);

            processState(enemy, seesPlayer, delta, events);
        }
        return events;
    }

    private void updateTimers(Enemy enemy, float delta) {
        enemy.updateReactionTimer(delta);
        enemy.updateAimMemoryTimer(delta);
        enemy.updateShotCommitTimer(delta);
        if (enemy.getCurrentWeapon() != null) {
            enemy.getCurrentWeapon().updateCooldown(delta);
        }
    }

    private void processState(Enemy enemy, boolean seesPlayer, float delta, List<GameEvent> events) {
        tryOpenDoorInPath(enemy);
        switch (enemy.getCurrentState()) {
            case PATROL -> updatePatrol(enemy, seesPlayer, delta, events);
            case ATTACK -> updateAttack(enemy, seesPlayer, delta, events);
            case SEARCH -> updateSearch(enemy, seesPlayer, delta, events);
            case INVESTIGATE -> updateInvestigate(enemy, seesPlayer, delta, events);
        }
    }

    private void updateInvestigate(Enemy enemy, boolean seesPlayer, float delta, List<GameEvent> events) {
        if (seesPlayer) {
            enterAttack(enemy);
            return;
        }

        if (!enemy.getCurrentPath().isEmpty()) {
            moveAlongPath(enemy, enemy.getProfile().getChaseSpeed(), delta, true, true);
            if (enemy.getCurrentPath().isEmpty()) {
                enemy.resetAimMemoryTimer();
            }
            return;
        }

        if (enemy.isAimMemoryTimer()) {
            returnToPatrol(enemy);
            return;
        }
        performSearchRotation(enemy, delta);
    }

    private void updatePatrol(Enemy enemy, boolean seesPlayer, float delta, List<GameEvent> events) {
        if (seesPlayer) {
            enterAttack(enemy);
            return;
        }
        if (enemy.getCurrentPath().isEmpty()) {
            goToNextPatrolPoint(enemy);
        }

        moveAlongPath(enemy, enemy.getProfile().getPatrolSpeed(), delta, false, true);
    }

    private void updateAttack(Enemy enemy, boolean seesPlayer, float delta, List<GameEvent> events) {
        enemy.setIntendMove(0,0);
        if (!seesPlayer) {
            enemy.setReactionTimer(0f);
            float targetAngle = (float) Math.toDegrees(Math.atan2(enemy.getLastKnownPlayerY() - enemy.getY(), enemy.getLastKnownPlayerX() - enemy.getX()));
            enemy.rotateTowards(targetAngle, delta);
            if (enemy.isAimMemoryTimer()) {
                enterSearch(enemy);
            }
            return;
        }
        enemy.setLastKnownPlayerPosition(player.getX(), player.getY());
        enemy.resetAimMemoryTimer();
        AimBehavior behavior = aimBehaviors.get(enemy.getProfile().getAimBehaviorType());
        if (behavior != null) {
            behavior.updateAim(enemy, player, delta);
            }
        updatePathToPlayer(enemy, delta);
        moveAlongPath(enemy, enemy.getProfile().getChaseSpeed(), delta, true, false);

        float distToPlayer = new Vec2(enemy.getX(), enemy.getY())
                .distanceTo(player.getX(), player.getY());

        float maxAttackDist = Math.min(
                enemy.getProfile().getPreferredAttackRange(),
                enemy.getCurrentWeapon().getDefinition().getRange()
        );
        // стріляємо тільки якщо є шанс попасти, тобто якщо гравець не задалеко
        if (distToPlayer > maxAttackDist) {
            enemy.setShotCommitStarted(false);
            return;
        }



        Weapon weapon = enemy.getCurrentWeapon();
        if (weapon == null || !weapon.canFire()) return;
        if (!enemy.isReactionTimer()) return;

        if (!enemy.isShotCommitStarted()) {
            // зафіксували ціль
            enemy.resetShotCommitTimer();
            enemy.setShotCommitStarted(true);
            return;
        }
        if (!enemy.isShotCommitTimer()) return;
        float baseAngle = enemy.getFacingAngle();
        if (behavior != null){
            baseAngle += behavior.calculateError(enemy, player,
                    new Vec2(enemy.getX(), enemy.getY()).distanceTo(player.getX(), player.getY()));
        }
        Vec2 from = new Vec2(enemy.getX(), enemy.getY());
        Vec2 target = Vec2.fromAngleDeg(baseAngle);
        WeaponFireContext weaponFireContext = new WeaponFireContext(
                rayCastSystem, enemy, from,
                target, enemy.getCurrentWeapon().getDefinition().getRange(),
                Set.of(enemy), enemy.getCurrentWeapon().getDefinition().getDamage(),
                weapon.getDefinition().getKnockbackForce()
        );
        events.addAll(weaponSystem.useWeapon(weaponFireContext, enemy.getCurrentWeapon()));
        enemy.resetReactionTimer();
        enemy.setShotCommitStarted(false);
        }

    private void updateSearch(Enemy enemy, boolean seesPlayer, float delta, List<GameEvent> events) {
        if (seesPlayer) {
            applyMemoryReaction(enemy);
            enterAttack(enemy);
            return;
        }

        if (!enemy.getCurrentPath().isEmpty()) {
            moveAlongPath(enemy, enemy.getProfile().getChaseSpeed(), delta, true, true);
            if (enemy.getCurrentPath().isEmpty()) {
                enemy.resetAimMemoryTimer();
            }
            return;
        }

        if (enemy.isAimMemoryTimer()) {
            returnToPatrol(enemy);
            return;
        }
        performSearchRotation(enemy, delta);

    }

    public void onSoundHeard(Enemy enemy, float worldX, float worldY) {
        if (enemy.getCurrentState() == AIState.ATTACK) return;
        enemy.setLastKnownPlayerPosition(worldX, worldY);
        enemy.changeState(AIState.INVESTIGATE);
        enemy.setAimMemoryTimer(0);

        List<Vec2> path = pathfinder.findPath(enemy, worldX, worldY, List.of(player));
        if (!path.isEmpty()) enemy.setCurrentPath(path);

    }

    private void updatePathToPlayer(Enemy enemy, float delta) {
        Float last = lastPathUpdate.get(enemy);
        if (last != null && last > 0f) {
            lastPathUpdate.put(enemy, last - delta);
            return;
        }

        List<Vec2> currentPath = enemy.getCurrentPath();
        boolean needNewPath = currentPath == null || currentPath.isEmpty();

        if (!needNewPath) {
            Vec2 lastPoint = currentPath.get(currentPath.size() - 1);
            float dx = lastPoint.x - player.getX();
            float dy = lastPoint.y - player.getY();
            float tileSize = pathfinder.getTitleSize();
            if (Math.sqrt(dx * dx + dy * dy) > REPAT_DISTANCE_THRESHOLD * tileSize) {
                // якщо гравець відійшов на певну кількість тайлів то перебудовуємо шлях
                needNewPath = true;
            }
        }

        if (needNewPath) {
            List<Vec2> newPath = pathfinder.findPath(enemy, player.getX(), player.getY(), List.of(player));
            if (!newPath.isEmpty()) enemy.setCurrentPath(newPath);
        }

        lastPathUpdate.put(enemy, ATTACK_REPEAT_INTERVAL);
    }

    private void moveAlongPath(Enemy enemy, float speed, float delta, boolean allowStrafe,
                               boolean canChangeAngle) {
        List<Vec2> path = enemy.getCurrentPath();
        if (path == null || path.isEmpty()) {
            enemy.setIntendMove(0, 0);
            return;
        }

        Vec2 target = path.get(0);
        float dx = target.x - enemy.getX();
        float dy = target.y - enemy.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < PIXEL_TOLERANCE) {
            path.removeFirst();
            if (path.isEmpty()) {
                enemy.setIntendMove(0, 0);
                return;
            }
            target = path.get(0);
            dx = target.x - enemy.getX();
            dy = target.y - enemy.getY();
            dist = (float) Math.sqrt(dx * dx + dy * dy);
        }
        float targetAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (canChangeAngle)
            enemy.rotateTowards(targetAngle, delta);

        // якщо не дозволений стрейф і недостатньо повернулись то стоїмо на місці
        if (!allowStrafe && Math.abs(Vec2.angleDiff(enemy.getFacingAngle(), targetAngle)) > DEGREE_TOLERANCE) {
            enemy.setIntendMove(0, 0);
            return;
        }

        float moveX = (dx / dist) * speed * delta;
        float moveY = (dy / dist) * speed * delta;
        enemy.setIntendMove(moveX, moveY);
    }

    private void enterAttack(Enemy enemy) {
        enemy.changeState(AIState.ATTACK);
        enemy.resetReactionTimer();
        lastPathUpdate.remove(enemy); // негайно перебудувати шлях
    }
    private void enterSearch(Enemy enemy){
        enemy.setLastKnownPlayerPosition(player.getX(), player.getY());
        enemy.resetAimMemoryTimer();
        enemy.changeState(AIState.SEARCH);
        List<Vec2> path = pathfinder.findPath(enemy, player.getX(), player.getY(), List.of(player));
        if (!path.isEmpty()) enemy.setCurrentPath(path);
    }
    private void applyMemoryReaction(Enemy enemy) {
        if (!enemy.isAimMemoryTimer()) {
            enemy.setReactionTimer(0f); // пам'ятає — миттєво
        } else {
            enemy.setReactionTimer(enemy.getProfile().getReactionTime() * 0.5f);
        }
    }

    private void performSearchRotation(Enemy enemy, float delta) {
        float currentAngle = enemy.getFacingAngle();
        float targetAngle = enemy.getSearchAngle();

        if (Math.abs(Vec2.angleDiff(currentAngle, targetAngle)) < DEGREE_TOLERANCE) {
            targetAngle = currentAngle + 90f + (float) Math.random() * 180f;
            enemy.setSearchAngle(targetAngle);
        }

        enemy.rotateTowards(targetAngle, delta);
    }

    private void buildPathToPatrolTarget(Enemy enemy) {
        Vec2 target = enemy.getCurrentPatrolTarget();
        if (target == null) {
            enemy.setCurrentPath(List.of());
            return;
        }
        List<Vec2> path = pathfinder.findPath(enemy, target.x, target.y, List.of(player));
        if (!path.isEmpty()) {
            enemy.setCurrentPath(path);
            return;
        }
        enemy.goToNextPatrolPoint();
        buildPathToPatrolTarget(enemy);
    }

    private void goToNextPatrolPoint(Enemy enemy) {
        enemy.goToNextPatrolPoint();
        buildPathToPatrolTarget(enemy);
    }

    private void returnToPatrol(Enemy enemy) {
        enemy.changeState(AIState.PATROL);
        buildPathToPatrolTarget(enemy);
    }
    public void onEnemyHit(Enemy enemy, Entity hitter){
        if (enemy.getCurrentState() == AIState.ATTACK) return;
        enterSearch(enemy);
    }
    private void tryOpenDoorInPath(Enemy enemy){
        List<Vec2> path = enemy.getCurrentPath();
        if (path == null || path.isEmpty()) return;

        Vec2 nextPoint = path.get(0);
        float ts = worldGeometry.getTileSize();
        int tileX = (int) Math.floor(nextPoint.x / ts);
        int tileY = (int) Math.floor(nextPoint.y / ts);
        float tileCX = tileX * ts + ts / 2f;
        float tileCY = tileY * ts + ts / 2f;
        Door door = findDoorAt(tileCX, tileCY);
        if (door == null) return;
        float distToDoor = new Vec2(enemy.getX(), enemy.getY()).distanceTo(door.getX(), door.getY());
        if (distToDoor <= INTERACTION_RADIUS){
            door.requestOpen();
        }
    }
    private Door findDoorAt(float worldX, float worldY) {
        for (Door door : doors) {
            if (door.getBounds().contains(worldX, worldY)) return door;
        }
        return null;
    }
}