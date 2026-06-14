package org.core.controller;

import lombok.Getter;
import org.content.aim_behavior.StandardAim;
import org.content.registry.EnemyProfileRegistry;
import org.content.registry.WeaponRegistry;
import org.content.weapon_behavior.SimpleRayCastBehavior;
import org.core.ai.EnemyAI;
import org.core.ai.PathFinder;
import org.core.ai.VisionSystem;
import org.core.behavior.AimBehavior;
import org.core.collision.Blocker;
import org.core.collision.CollisionSystem;
import org.core.data.DoorData;
import org.core.data.EnemySpawnData;
import org.core.data.LevelData;
import org.core.data.WeaponPickupData;
import org.core.definition.EnemyProfile;
import org.core.definition.PlayerProfile;
import org.core.entity.Door;
import org.core.entity.Enemy;
import org.core.entity.Player;
import org.core.entity.WeaponPickup;
import org.core.enums.*;
import org.core.event.*;
import org.core.math.Vec2;
import org.core.raycast.RayCastSystem;
import org.core.state.GameStateView;
import org.core.state.LevelState;
import org.core.weapon.Weapon;
import org.core.definition.WeaponDefinition;
import org.core.weapon.WeaponFireContext;
import org.core.weapon.WeaponSystem;

import java.util.*;

public class GameController {

    private LevelState levelState;
    @Getter
    private GameStateView stateView;
    private CollisionSystem collisionSystem;

    // Команди гравця на поточний кадр
    private float pendingDx, pendingDy;
    private float pendingAimX, pendingAimY;
    private boolean pendingShoot;
    private boolean pendingDrop;
    private boolean pendingInteract;
    private MovementMode pendingMovementMode;
    private final WeaponSystem weaponSystem;
    private RayCastSystem rayCastSystem;

    private VisionSystem visionSystem;
    private EnemyAI enemyAI;
    private PathFinder pathfinder;

    private final WeaponRegistry weaponRegistry;
    private final EnemyProfileRegistry enemyProfileRegistry;
    private final Map<AimBehaviorType, ?> aimBehaviors;

    private final static float FALL_BODY_SOUND = 250;

    public GameController(WeaponRegistry weaponRegistry,
                          EnemyProfileRegistry enemyProfileRegistry,
                          Map<AimBehaviorType, ?> aimBehaviors, WeaponSystem weaponSystem) {
        this.weaponRegistry = weaponRegistry;
        this.enemyProfileRegistry = enemyProfileRegistry;
        this.aimBehaviors = aimBehaviors;
        this.pendingMovementMode = MovementMode.WALK;
        this.weaponSystem = weaponSystem;
    }


    public void loadLevel(LevelData data) {

        List<Door> doors = new ArrayList<>();
        List<Blocker> blockers = new ArrayList<>();

        for (DoorData dd : data.doors) {
            Door door = new Door(dd.doorId, dd.x, dd.y, dd.width, dd.height, dd.initialState, dd.orientation);
            doors.add(door);
            blockers.add(door);
        }
        // поки заглушка, потім обробити з спавнпойнтів
        List<Enemy> enemies = new ArrayList<>();
        for (EnemySpawnData d : data.enemySpawns) {
            // сортуємо, щоб patrolPathId ворога = pathId вейпойнтів
            List<Vec2> patrolPath = data.waypoints.stream()
                    .filter(w -> w.pathId.equals(d.patrolPathId))
                    .sorted(Comparator.comparingInt(w -> w.order))
                    .map(w -> new Vec2(w.x, w.y))
                    .toList();
            // create enemy profile and weapon based on spawn data
            EnemyProfile profile = enemyProfileRegistry.get(d.enemyTypeId);
            // use registered weapon if provided, otherwise fall back to default
            Weapon weapon;
            try {
                weapon = new Weapon(weaponRegistry.get(d.weaponId), true);
            } catch (Exception ex) {
                // fallback to a simple default weapon (was previously hardcoded)
                weapon = new Weapon(new WeaponDefinition(
                        "1",
                        WeaponType.HITSCAN,
                        100,
                        2000f,
                        1f,
                        100,
                        false,
                        true,
                        new SimpleRayCastBehavior(),
                        10,
                        0
                ), true);
            }
            enemies.add(new Enemy(
                    d.x, d.y,
                    profile,
                    weapon,
                    d.enemyId,
                    patrolPath,
                    d.facingAngle
            ));
        }
        blockers.addAll(enemies);

        List<WeaponPickup> pickups = new ArrayList<>();
        for (WeaponPickupData w : data.weaponPickups) {
            pickups.add(new WeaponPickup(
                    w.x, w.y, weaponRegistry.get(w.weaponId).getId(), new Weapon(weaponRegistry.get(w.weaponId)), false,  UUID.randomUUID().toString()
            ));
        }
        rayCastSystem = new RayCastSystem(data.worldGeometry, blockers);
        Player player = new Player(
                data.playerSpawn.x, data.playerSpawn.y,
                16f, 16f,
                new PlayerProfile("1",
                        new Weapon(weaponRegistry.get("Famae")),
                        new Weapon(weaponRegistry.get("Knife")),
                        0, 1, 1, 1, 0, 1
                ));
        player.setMovementMode(pendingMovementMode);
        blockers.add(player);

        GoalType goalType = data.goalType;
        if (levelState == null) {
            levelState = new LevelState();
        }
        levelState.reset(data.worldGeometry, player, enemies, doors, pickups,
                data.exits, goalType, data.targetEnemyId);

        collisionSystem = new CollisionSystem(data.worldGeometry, blockers);

        stateView = new GameStateView(levelState);


        visionSystem = new VisionSystem(rayCastSystem, data.worldGeometry);
        pathfinder = new PathFinder(data.worldGeometry, collisionSystem, blockers);
        Map<AimBehaviorType, AimBehavior> aimBehaviorMap = new HashMap<>();
        aimBehaviorMap.put(AimBehaviorType.STANDARD, new StandardAim());
        enemyAI = new EnemyAI(visionSystem, levelState.getEnemies(), levelState.getPlayer(), pathfinder, weaponSystem, rayCastSystem ,
                aimBehaviorMap, doors, levelState.getWorldGeometry());
        clearPendingCommands();


    }

    public void update(float delta) {
        // трошки костилів, якщо падає фреймрейт щоб ентіті не телепортувались сильно
        // тоді гра уповільниться просто
        // потенційно придумати альтернативу, наприклад накопичувальну систему що викличе
        // update декілька разів підряд якщо delta занадто велике (але такий підхід теж має проблеми)
        delta = Math.min(delta, 0.05f);
        if (!levelState.isPlaying()) {
            return;
        }

        Player player = levelState.getPlayer();

        // Крок 2. Оновити player movement intent, викликається не тут

        // Aim (оновлення facingAngle)
        if (pendingAimX != player.getX() || pendingAimY != player.getY()) {
            player.aimAt(pendingAimX, pendingAimY);
        }

        for (Enemy enemy : levelState.getEnemies()) {
            enemy.resetDamageFlag();
        }

        List<GameEvent> aIEvents = enemyAI.update(delta, levelState.getSoundEventQueue());
        levelState.addAllGameEvent(aIEvents);


        for (Enemy enemy : levelState.getEnemies()) {
            if (Math.abs(enemy.getVelocityX()) >= 0.01f || Math.abs(enemy.getVelocityY()) >= 0.01f) {
                collisionSystem.applyKnockback(enemy, delta, CollisionSystem.DEFAULT_FRICTION);
                if (Math.abs(enemy.getVelocityX()) < 0.5f && Math.abs(enemy.getVelocityY()) < 0.5f) {
                    enemy.setVelocity(0, 0);

                }

            } else {
                collisionSystem.move(enemy, enemy.getIntendedDx(), enemy.getIntendedDy());
            }
        }

        if (pendingDx != 0f || pendingDy != 0f) {
            collisionSystem.move(player, pendingDx, pendingDy);
            levelState.getSoundEventQueue().add(
                    new SoundEvent(player.getX(), player.getY(),
                            player.getMovementMode().getSound() * player.getMoveSoundModify(),
                            player)
            );
        }
        // додати для інших ентіті коли вони з'являться


        if (pendingInteract) {
            InteractionSystem.interact(player, levelState, rayCastSystem);
            pendingInteract = false;
        }
        if (pendingDrop){
            Optional<WeaponPickup> weaponPickup = player.dropWeapon();
            if (weaponPickup.isPresent()){
                levelState.getPickups().add(weaponPickup.get());
            }
        }

        // Крок 8b DoorSystem
        for (Door door : levelState.getDoors()) {
            door.update(delta);
        }

        // Крок 9. WeaponSystem – cooldowns, hitscan, apply damage
        player.getCurrentWeapon().updateCooldown(delta);
        if (pendingShoot) {
            Weapon weapon = player.getCurrentWeapon();
            if (weapon.canFire()) {
                Vec2 from = new Vec2(player.getX(), player.getY());
                Vec2 target = Vec2.fromAngleDeg(player.getFacingAngle());
                WeaponFireContext context = new WeaponFireContext(
                        rayCastSystem, player, from, target,
                        weapon.getDefinition().getRange(),
                        Set.of(player),
                        weapon.getDefinition().getDamage() + player.getBonusDamage(),
                        weapon.getDefinition().getKnockbackForce(),
                        weapon.getDefinition().getSpread(),
                        levelState.getSoundEventQueue()
                );
                List<GameEvent> weaponEvents = weaponSystem.useWeapon(context, weapon);
                levelState.addAllGameEvent(weaponEvents);
            }
            pendingShoot = false;
        }
        // Крок 10. Перевірити deaths → перемістити в corpses
        List<Enemy> deadEnemies = levelState.flushDeadEnemies();
        List<GameEvent> deadEvent = new ArrayList<>();
        for (Enemy enemy : deadEnemies) {
            deadEvent.add(new EnemyDiedEvent(enemy.getX(), enemy.getY(), enemy.getEnemyId()));
            levelState.getSoundEventQueue().add(
                    new SoundEvent(
                            enemy.getX(), enemy.getY(),
                            FALL_BODY_SOUND, enemy
                    )
            );
            if (enemy.getCurrentWeapon() != null) {
                WeaponDefinition definition = enemy.getCurrentWeapon().getDefinition();
                Weapon weapon = new Weapon(definition);
                WeaponPickup pickup = new WeaponPickup(enemy.getX(), enemy.getY(),
                        definition.getId(), weapon, false, UUID.randomUUID().toString());
                levelState.getPickups().add(pickup);
            }
        }
        levelState.addAllGameEvent(deadEvent);
        for (Enemy enemy : levelState.getEnemies()) {
            if (enemy.isDamaged()) {
                levelState.addAllGameEvent(enemyAI.onEnemyHit(enemy, player));
            }
        }

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
    public void drop(){
        pendingDrop = true;
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
        pendingDrop = false;
    }

    public List<Enemy> getEnemies() {
        if (levelState != null && levelState.getEnemies() != null) {
            return levelState.getEnemies();
        }
        return Collections.emptyList();
    }

    public List<Door> getDoors() {
        return levelState.getDoors();
    }

    public List<WeaponPickup> getPickups() {
        return levelState.getPickups();
    }

    public Player getPlayer() {
        return levelState.getPlayer();
    }
}