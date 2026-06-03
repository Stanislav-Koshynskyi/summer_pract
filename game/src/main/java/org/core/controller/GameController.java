package org.core.controller;

import java.util.*;

import lombok.Getter;
import org.core.collision.Blocker;
import org.core.collision.CollisionSystem;
import org.core.data.*;
import org.core.entity.*;
import org.core.enums.*;
import org.core.event.*;

import org.core.state.GameStateView;
import org.core.state.LevelState;
public class GameController {

    private LevelState levelState;
    // ---------- Доступ до стану ----------
    @Getter
    private GameStateView stateView;
    private CollisionSystem collisionSystem;

    // Команди гравця на поточний кадр
    private float pendingDx, pendingDy;
    private float pendingAimX, pendingAimY;
    private boolean pendingShoot;
    private boolean pendingInteract;
    private MovementMode pendingMovementMode;

    // Заглушки для реєстрів (поки не використовуються, але будуть потрібні)
    private final Object weaponRegistry;
    private final Object enemyProfileRegistry;
    private final Map<AimBehaviorType, ?> aimBehaviors;

    public GameController(Object weaponRegistry,
                          Object enemyProfileRegistry,
                          Map<AimBehaviorType, ?> aimBehaviors) {
        this.weaponRegistry = weaponRegistry;
        this.enemyProfileRegistry = enemyProfileRegistry;
        this.aimBehaviors = aimBehaviors;
        this.pendingMovementMode = MovementMode.WALK;
    }


    public void loadLevel(LevelData data) {
        Player player = new Player(
                data.playerSpawn.x, data.playerSpawn.y,
                16f, 16f,
                null // defaultWeapon буде підставлено пізніше, коли з'явиться WeaponRegistry
        );
        player.setMovementMode(pendingMovementMode);

        List<Door> doors = new ArrayList<>();
        List<Blocker> blockers = new ArrayList<>();

        for (DoorData dd : data.doors) {
            Door door = new Door(dd.doorId, dd.x, dd.y, dd.width, dd.height, dd.initialState);
            doors.add(door);
            blockers.add(door);
        }
        // поки заглушка, потім обробити з спавнпойнтів
        List<Enemy> enemies = new ArrayList<>();

        // сейм
        List<WeaponPickup> pickups = new ArrayList<>();

        GoalType goalType = data.goalType;
        if (levelState == null) {
            levelState = new LevelState();
        }
        levelState.reset(data.worldGeometry, player, enemies, doors, pickups,
                goalType, data.targetEnemyId);

        collisionSystem = new CollisionSystem(data.worldGeometry, blockers);

        stateView = new GameStateView(levelState);

        clearPendingCommands();
    }

    public void update(float delta) {
        // трошки костилів, якщо падає фреймрейт щоб ентіті не телепортувались сильно
        // тоді гра уповільниться просто
        // потенційно придумати альтернативу, наприклад накопичувальну систему що викличе
        // update декілька разів підряд якщо delta занадто велике (але такий підхід теж має проблеми)
        delta = Math.min (delta, 0.05f);
        if (!levelState.isPlaying()) {
            return;
        }

        Player player = levelState.getPlayer();

        // Крок 2. Оновити player movement intent, викликається не тут

        // Aim (оновлення facingAngle)
        if (pendingAimX != player.getX() || pendingAimY != player.getY()) {
            player.aimAt(pendingAimX, pendingAimY);
        }
        //TODO
        // Крок 3. FootstepEmitter – згенерувати footstep SoundEvents
        // Крок 4. HearingSystem – обробити SoundEventQueue
        // Крок 5. VisionSystem – оновити видимість
        // Крок 6. EnemyAI – оновити FSM, path target, movement intent і fire requests

        // Крок 7. CollisionSystem – застосувати player/enemy movement і knockback

        if (pendingDx != 0f || pendingDy != 0f) {
            collisionSystem.move(player, pendingDx, pendingDy);
        }
        // додати для інших ентіті коли вони з'являться


        if (pendingInteract) {
            // TODO: InteractionSystem.interact(player)
            pendingInteract = false;
        }

        // Крок 8b DoorSystem
        for (Door door : levelState.getDoors()) {
            door.update(delta);
        }

        // Крок 9. WeaponSystem – cooldowns, hitscan, apply damage
        // TODO: MVP-0 – реалізувати hitscan

        if (pendingShoot) {
            // TODO: WeaponSystem.shoot(player)
            pendingShoot = false;
        }
        // Крок 10. Перевірити deaths → перемістити в corpses
        levelState.flushDeadEnemies();

        // Перевірка смерті гравця
        if (!player.isAlive()) {
            levelState.setPhase(GamePhase.PLAYER_DEAD);
            levelState.addGameEvent(new PlayerDiedEvent(player.getX(), player.getY()));
        }

        // Крок 11. Оновити LevelStats і перевірити LevelGoal
        // TODO: MVP-2 – розширити статистику (alerted enemies, silent kills тощо)
        checkLevelGoals();
        levelState.getStats().addElapsedTime(delta);



        // Очищення команд гравця після обробки
        clearPendingCommands();
    }

    public void movePlayer(float dx, float dy) {
        this.pendingDx = dx;
        this.pendingDy = dy;
    }

    public void aimPlayer(float worldX, float worldY) {
        this.pendingAimX = worldX;
        this.pendingAimY = worldY;
    }

    public void shoot() {
        pendingShoot = true;
    }

    public void interact() {
        pendingInteract = true;
    }

    public void setMovementMode(MovementMode mode) {
        this.pendingMovementMode = mode;
        Player player = levelState.getPlayer();
        if (player != null) {
            player.setMovementMode(mode);
        }
    }

    public List<GameEvent> drainEvents() {
        List<GameEvent> events = levelState.getGameEvents();
        levelState.clearGameEvents();
        return events;
    }

    private void checkLevelGoals() {
        if (levelState.getPhase() != GamePhase.PLAYING) {
            return;
        }

        GoalType goal = levelState.getGoalType();
        boolean completed = false;
        LevelOutcome outcome = LevelOutcome.MIXED;

        switch (goal) {
            case KILL_ALL:
                if (levelState.areAllEnemiesDead()) {
                    completed = true;
                    outcome = determineOutcome();
                }
                break;
            case ESCAPE:
                if (levelState.isPlayerAtExit()) {
                    completed = true;
                    outcome = determineOutcome();
                }
                break;
            case KILL_TARGET:
                if (levelState.getTargetEnemyId() != null &&
                        !levelState.isEnemyAlive(levelState.getTargetEnemyId())) {
                    completed = true;
                    outcome = determineOutcome();
                }
                break;
        }

        if (completed) {
            levelState.setPhase(GamePhase.LEVEL_COMPLETED);
            levelState.addGameEvent(new LevelCompletedEvent(outcome));
        }

    }

    private LevelOutcome determineOutcome() {
        // Заглушка: для MVP-0 завжди MIXED, бо немає статистики
        return LevelOutcome.MIXED;
    }

    private void clearPendingCommands() {
        pendingDx = 0f;
        pendingDy = 0f;
        pendingShoot = false;
        pendingInteract = false;
    }
}