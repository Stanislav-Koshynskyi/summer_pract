package org.core.ai;

import org.core.entity.Enemy;
import org.core.entity.Entity;
import org.core.entity.Player;
import org.core.enums.AIState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnemyAI {

    private final VisionSystem visionSystem;
    private final List<Enemy> enemies;
    private final Player player;

    public EnemyAI(VisionSystem visionSystem, List<Enemy> enemies, Player player) {
        this.visionSystem = visionSystem;
        this.enemies = enemies;
        this.player = player;
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
            case PATROL -> updatePatrol(enemy, seesPlayer);
            case ATTACK -> updateAttack(enemy, seesPlayer, delta);
            case SEARCH -> updateSearch(enemy, seesPlayer, delta);
            case INVESTIGATE -> updateInvestigate(enemy, seesPlayer, delta);
        }
    }

    private void updateInvestigate(Enemy enemy, boolean seesPlayer, float delta) {
        if (seesPlayer) {
            enemy.changeState(AIState.ATTACK);
            enemy.resetReactionTimer();
            return;
        }
        //todo рух до точки звуку
        if (enemy.isAimMemoryTimer()) {
            enemy.changeState(AIState.PATROL);
        }
    }

    private void updatePatrol(Enemy enemy, boolean seesPlayer) {
        // TODO: рух по waypoints
        if (seesPlayer) {
            enemy.changeState(AIState.ATTACK);
            enemy.resetReactionTimer(); // повна затримка
        }
    }

    private void updateAttack(Enemy enemy, boolean seesPlayer, float delta) {
        if (!seesPlayer) {
            // Гравець зник
            enemy.setLastKnownPlayerPosition(player.getX(), player.getY());
            enemy.resetAimMemoryTimer();
            enemy.changeState(AIState.SEARCH);
            return;
        }
        // TODO: переслідування
        // Гравець на виду
        if (enemy.isReactionTimer()) {

            // TODO: стрільба
        }
    }

    private void updateSearch(Enemy enemy, boolean seesPlayer, float delta) {
        if (seesPlayer) {
            // Знову побачив гравця
            if (!enemy.isAimMemoryTimer()) {
                // Ще пам'ятає — миттєва реакція
                enemy.setReactionTimer(0f);
            } else {
                enemy.setReactionTimer(enemy.getProfile().getReactionTime() * 0.5f);
            }
            enemy.changeState(AIState.ATTACK);
            return;
        }

        // Не бачить гравця
        if (enemy.isAimMemoryTimer()) {
            // Пам'ять вичерпалась
            enemy.changeState(AIState.PATROL);
            return;
        }

        // TODO: рух до lastKnownPlayerPosition
    }

    public void onSoundHeard(Enemy enemy, float worldX, float worldY) {
        if (enemy.getCurrentState() == AIState.ATTACK) return;
        enemy.setLastKnownPlayerPosition(worldX, worldY);
        enemy.changeState(AIState.INVESTIGATE);
        enemy.resetAimMemoryTimer();
    }
}