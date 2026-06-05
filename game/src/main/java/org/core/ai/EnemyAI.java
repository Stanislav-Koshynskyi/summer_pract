package org.core.ai;

import org.core.collision.PathBlocker;
import org.core.entity.Enemy;
import org.core.entity.Entity;
import org.core.entity.Player;
import org.core.enums.AIState;
import org.core.math.Vec2;

import java.util.*;

public class EnemyAI {

    // Як часто перебудовувати шлях у ATTACK (секунди)
    private static final float ATTACK_REPEAT_INTERVAL = 0.1f;
    // Мінімальна зміна позиції гравця для перебудови шляху (тайлів)
    private static final float REPAT_DISTANCE_THRESHOLD = 2f;

    private static final float PIXEL_TOLERANCE = 5;
    private static final float DEGREE_TOLERANCE = 10;

    // коли останній раз міняли шлях, треба щоб не рахувати шлях дуже часто
    private final Map<Enemy, Float> lastPathUpdate = new HashMap<>();

    private final VisionSystem visionSystem;
    private final List<Enemy> enemies;
    private final Player player;
    private final PathFinder pathfinder;

    public EnemyAI(VisionSystem visionSystem, List<Enemy> enemies, Player player, PathFinder pathfinder) {
        this.visionSystem = visionSystem;
        this.enemies = enemies;
        this.player = player;
        this.pathfinder = pathfinder;
    }

    public void update(float delta) {
        Set<Entity> ignoredEntities = new HashSet<>(enemies);
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            updateTimers(enemy, delta);

            boolean seesPlayer = visionSystem.canEnemySeePlayer(enemy, player, ignoredEntities);

            processState(enemy, seesPlayer, delta);
        }
    }

    private void updateTimers(Enemy enemy, float delta) {
        enemy.updateReactionTimer(delta);
        enemy.updateAimMemoryTimer(delta);
        enemy.updateShotCommitTimer(delta);
        if (enemy.getCurrentWeapon() != null) {
            enemy.getCurrentWeapon().updateCooldown(delta);
        }
    }

    private void processState(Enemy enemy, boolean seesPlayer, float delta) {
        switch (enemy.getCurrentState()) {
            case PATROL -> updatePatrol(enemy, seesPlayer, delta);
            case ATTACK -> updateAttack(enemy, seesPlayer, delta);
            case SEARCH -> updateSearch(enemy, seesPlayer, delta);
            case INVESTIGATE -> updateInvestigate(enemy, seesPlayer, delta);
        }
    }

    private void updateInvestigate(Enemy enemy, boolean seesPlayer, float delta) {
        if (seesPlayer) {
            enterAttack(enemy);
            return;
        }

        if (!enemy.getCurrentPath().isEmpty()) {
            moveAlongPath(enemy, enemy.getProfile().getChaseSpeed(), delta, true);
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

    private void updatePatrol(Enemy enemy, boolean seesPlayer, float delta) {
        if (seesPlayer) {
            enterAttack(enemy);
            return;
        }
        if (enemy.getCurrentPath().isEmpty()){
            goToNextPatrolPoint(enemy);
        }

        moveAlongPath(enemy, enemy.getProfile().getPatrolSpeed(), delta, false);
    }

    private void updateAttack(Enemy enemy, boolean seesPlayer, float delta) {
        if (!seesPlayer) {
            // Гравець зник
            enemy.setLastKnownPlayerPosition(player.getX(), player.getY());
            enemy.resetAimMemoryTimer();
            enemy.changeState(AIState.SEARCH);
            List<Vec2> path = pathfinder.findPath(enemy, player.getX(), player.getY(), List.of(player));
            if (!path.isEmpty()) enemy.setCurrentPath(path);
            return;
        }

        if (enemy.isReactionTimer()) {

            // TODO: стрільба
        }
        updatePathToPlayer(enemy, delta);
        moveAlongPath(enemy, enemy.getProfile().getChaseSpeed(), delta, true);
    }

    private void updateSearch(Enemy enemy, boolean seesPlayer, float delta) {
        if (seesPlayer) {
            applyMemoryReaction(enemy);
            enterAttack(enemy);
            return;
        }

        if (!enemy.getCurrentPath().isEmpty()){
            moveAlongPath(enemy, enemy.getProfile().getChaseSpeed(), delta, true);
            if (enemy.getCurrentPath().isEmpty()){
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
        enemy.resetAimMemoryTimer();

        List<Vec2> path = pathfinder.findPath(enemy, worldX, worldY,List.of(player));
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

    private void moveAlongPath(Enemy enemy, float speed, float delta, boolean allowStrafe) {
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
    private void buildPathToPatrolTarget(Enemy enemy){
        Vec2 target = enemy.getCurrentPatrolTarget();
        if (target == null){
            enemy.setCurrentPath(List.of());
            return;
        }
        List<Vec2> path = pathfinder.findPath(enemy, target.x, target.y, List.of(player));
        if (!path.isEmpty()){
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
}