package org.client;

import com.badlogic.gdx.ApplicationAdapter;
///Changes
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import lombok.Setter;
///Changes
import org.client.menu.StartMenu;
import org.content.registry.ContentRegistries;
import org.content.registry.WeaponRegistry;
import org.content.registry.EnemyProfileRegistry;
import org.core.controller.GameController;
import org.core.data.*;
import org.core.definition.EnemyProfile;
import org.core.entity.*;
import org.core.enums.*;
import org.core.event.*;
import org.core.math.Vec2;
import org.core.state.*;
import org.core.weapon.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
///Changes
public class CoreGame extends Game {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private CameraInputController cameraController;
    private SpriteBatch spriteBatch;
    private Texture doorVerticalTexture;
    private Texture doorHorizontalTexture;
    private Texture playerSprite;
    private Texture alertTexture;
    private Texture searchTexture;
    private Texture corpseTexture;
    private Texture bulletTexture;
    private Texture weaponPickupTexture;
    @Setter
    private GameStateView gameStateView;
    private ShapeRenderer shapeRenderer;
    private GameController gameController;
    private WeaponSystem weaponSystem;
    private WeaponRegistry weaponRegistry;
    private EnemyProfileRegistry enemyProfileRegistry;
    private DeadBody playerCorpse;
    private AssetLoader assetLoader;
    private boolean debugMode = false;
    private final Vector3 mouseInWorld = new Vector3();
    private final List<VisualAttackEffect> attackEffects = new ArrayList<>();
    private final List<VisualAlertEffect> alertEffects = new java.util.ArrayList<>();
    private final List<BulletEffect> bulletEffects = new java.util.ArrayList<>();
    private final List<DeadBody> deadBodies = new ArrayList<>();
    private final Map<String, EnemyAnimData> enemyAnimMap = new java.util.HashMap<>();
    private final java.util.Map<Door, Float> doorAnimMap = new java.util.HashMap<>();
    private boolean isShooting = false;

    // Анімація
    private Animation<TextureRegion> walkAnimation;
    private Texture[] animationFrames;
    private float stateTime;
    private boolean isPlayerMoving = false;

    private Animation<TextureRegion> enemyWalkAnimation1;
    private Texture[] enemyAnimationFrames;

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        map = new TmxMapLoader().load("maps/level_01.tmx");
        float unitScale = 1f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1600, 900);
        camera.zoom = 0.5f;

        //cameraController = new CameraInputController(camera);
        //Gdx.input.setInputProcessor(cameraController);

        // тестовий профіль, щоб програма не падала
        EnemyProfile testEnemyProfile = new EnemyProfile(
                "1",
                100,
                120f,
                220f,
                300f,
                0.5f,
                0.1f,
                0.5f,
                3.0f,
                200f,
                AimBehaviorType.values()[0],
                90f,
                130f,
                360f,
                16f,
                16f,
                20,
                300
        );

        EnemyProfile testEnemyProfile2 = new EnemyProfile(
                "2",
                100,
                120f,
                220f,
                300f,
                0.5f,
                0.1f,
                0.5f,
                3.0f,
                200f,
                AimBehaviorType.values()[0],
                90f,
                130f,
                360f,
                16f,
                16f,
                20,
                300
        );

        weaponSystem = new WeaponSystem();
        ContentRegistries.initAll();
        weaponRegistry = new WeaponRegistry();
        enemyProfileRegistry = new EnemyProfileRegistry();
        enemyProfileRegistry.register(testEnemyProfile);
        enemyProfileRegistry.register(testEnemyProfile2);
        LevelTmxLoader levelLoader = new LevelTmxLoader();
        LevelData levelData = levelLoader.parseMapObjects(map);

        gameController = new GameController(weaponRegistry, enemyProfileRegistry, new java.util.HashMap<>(), weaponSystem);
        gameController.loadLevel(levelData);

        this.gameStateView = gameController.getStateView();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        doorVerticalTexture = new Texture(Gdx.files.internal("textures/door_closed_vertical.png"));
        doorHorizontalTexture = new Texture(Gdx.files.internal("textures/door_closed_horizontal.png"));
        playerSprite = new Texture(Gdx.files.internal("sprites/enemies/sprSwatBoss/sprSwatBossWalk/sprSwatBossWalk_1.png"));
        alertTexture = new Texture(Gdx.files.internal("textures/AlertEnemy.png"));
        searchTexture = new Texture(Gdx.files.internal("textures/SearchEnemy.png"));
        corpseTexture = new Texture(Gdx.files.internal("sprites/enemies/sprSwatBoss/sprSwatBossDie/sprSwatBossDie_34.png"));
        bulletTexture = new Texture(Gdx.files.internal("textures/bullet_4.png"));
        weaponPickupTexture = new Texture(Gdx.files.internal("textures/weapon_1.png"));
        //playerSprite.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Анімація гравця
        int frames = 6;
        animationFrames = new Texture[frames];
        TextureRegion[] walkFrames = new TextureRegion[frames];

        for (int i = 0; i < frames; i++) {
            Texture texture = new Texture(Gdx.files.internal("sprites/enemies/sprSwatBoss/sprSwatBossWalk/sprSwatBossWalk_" + (i + 1) + ".png"));

            animationFrames[i] = texture;
            walkFrames[i] = new TextureRegion(texture);
        }

        walkAnimation = new Animation<>(0.1f, walkFrames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 8f;

        assetLoader = new AssetLoader();
        assetLoader.load();
        setScreen(new StartMenu(this)); // показати меню при старті
    }



    /// Changes
    @Override
    public void render() {
        if (getScreen() != null) {
            // Якщо є активний екран (наприклад, стартове меню), малюємо його
            super.render();
        } else {
            // Якщо екрану немає, запускається твій стандартний ігровий цикл
            input();
            logic();
            draw();
        }
    }


    private void input() {
        // Placeholder
    }

    private void logic() {
        if (gameController == null) return;

        Vector2 movement = new Vector2();
        float speed = gameStateView.getPlayerSpeed();
        float dt = Gdx.graphics.getDeltaTime();
        float dx = 0f;
        float dy = 0f;

        for (DeadBody body : deadBodies) {
            body.stateTime += dt;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            debugMode = !debugMode;
        }

        if (gameStateView.isCurrentWeaponAutomatic()) {
            isShooting = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        } else {
            isShooting = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        }

        if (isShooting) {
            gameController.shoot();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            // підібрати зброю
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restart();
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            gameController.setMovementMode(MovementMode.RUN);
        } else if (Gdx.input.isKeyPressed(Input.Keys.C)) {
            gameController.setMovementMode(MovementMode.SNEAK);
        } else {
            gameController.setMovementMode(MovementMode.WALK);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) movement.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) movement.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.x -= 1;

        if (movement.len2() > 0) {
            movement.nor();
        }
        dx += movement.x * speed * dt;
        dy += movement.y * speed * dt;

        isPlayerMoving = (dx != 0 || dy != 0);

        gameController.movePlayer(dx, dy);
        mouseInWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseInWorld);
        gameController.aimPlayer(mouseInWorld.x, mouseInWorld.y);
        gameController.update(Gdx.graphics.getDeltaTime());

        Iterator<VisualAttackEffect> it = attackEffects.iterator();
        while (it.hasNext()) {
            VisualAttackEffect effect = it.next();
            effect.lifetime -= dt;
            if (effect.lifetime <= 0) {
                it.remove();
            }
        }

        Iterator<VisualAlertEffect> alertIt = alertEffects.iterator();
        while (alertIt.hasNext()) {
            VisualAlertEffect effect = alertIt.next();
            effect.lifetime -= dt;
            if (effect.lifetime <= 0 || !effect.enemy.isAlive()) {
                alertIt.remove();
            }
        }

        Iterator<BulletEffect> bulletIt = bulletEffects.iterator();
        while (bulletIt.hasNext()) {
            BulletEffect effect = bulletIt.next();
            effect.update(dt);
            if (effect.lifetime <= 0 || effect.isArrived()) {
                bulletIt.remove();
            }
        }

        List<GameEvent> events = gameController.drainEvents();
        for (GameEvent event : events) {
            if (event instanceof ShotFiredEvent) {
                ShotFiredEvent shot = (ShotFiredEvent) event;

                float shooterAngle = 0f;
                boolean isPlayerShooter = true;

                for (Vec2 targetPos : shot.getTargets()) {
                    attackEffects.add(new VisualAttackEffect(
                            shot.fromX,
                            shot.fromY,
                            targetPos.x,
                            targetPos.y,
                            0.3f,
                            Color.YELLOW
                    ));
                }

                Enemy shooter = gameController.getEnemies().stream()
                        .filter(Enemy::isAlive)
                        .min((e1, e2) -> {
                            float d1 = (e1.getX() - shot.fromX) * (e1.getX() - shot.fromX) + (e1.getY() - shot.fromY) * (e1.getY() - shot.fromY);
                            float d2 = (e2.getX() - shot.fromX) * (e2.getX() - shot.fromX) + (e2.getY() - shot.fromY) * (e2.getY() - shot.fromY);
                            return Float.compare(d1, d2);
                        })
                        .orElse(null);

                if (shooter != null) {
                    float distSq = (shooter.getX() - shot.fromX) * (shooter.getX() - shot.fromX) + (shooter.getY() - shot.fromY) * (shooter.getY() - shot.fromY);
                    if (distSq < 2500f) {
                        shooterAngle = shooter.getFacingAngle();
                        isPlayerShooter = false;

                        EnemyAnimData data = enemyAnimMap.computeIfAbsent(shooter.getEnemyId(), k -> new EnemyAnimData());
                        data.currentState = AnimationState.ATTACK;
                        data.stateTime = 0f;
                    }
                }

                if (isPlayerShooter) {
                    shooterAngle = gameStateView.getPlayerFacingAngle();
                }

                for (Vec2 targetPos : shot.getTargets()) {
                    float ddx = targetPos.x - shot.fromX;
                    float ddy = targetPos.y - shot.fromY;
                    float distanceToTarget = (float) Math.sqrt(ddx * ddx + ddy * ddy);

                    float forwardOffset = 22f;
                    float sideOffset = 0f;

                    if (distanceToTarget < forwardOffset) {
                        forwardOffset = Math.max(0f, distanceToTarget - 2f);
                        sideOffset = 0f;
                    }

                    Vector2 spawnOffset = new Vector2(forwardOffset, sideOffset).rotateDeg(shooterAngle);
                    float actualFromX = shot.fromX + spawnOffset.x;
                    float actualFromY = shot.fromY + spawnOffset.y;

                    float bulletSpeed = 3000f;
                    float bulletLifetime = 1.2f;

                    bulletEffects.add(new BulletEffect(
                            actualFromX,
                            actualFromY,
                            targetPos.x,
                            targetPos.y,
                            bulletSpeed,
                            bulletLifetime
                    ));
                }
            }

            if (event instanceof EnemyAlertedEvent) {
                EnemyAlertedEvent alertedEvent = (EnemyAlertedEvent) event;
                Enemy enemy = gameController.getEnemies().stream()
                        .filter(e -> e.getEnemyId().equals(alertedEvent.getEnemyId()))
                        .findFirst()
                        .orElse(null);

                if (enemy != null && enemy.isAlive()) {
                    boolean alreadyHasMark = false;
                    for (VisualAlertEffect effect : alertEffects) {
                        if (effect.enemy == enemy) {
                            alreadyHasMark = true;
                            break;
                        }
                    }
                    if (!alreadyHasMark) {
                        alertEffects.add(new VisualAlertEffect(enemy, 1.5f));
                    }
                }
            }

            if (event instanceof EnemyDiedEvent) {
                EnemyDiedEvent diedEvent = (EnemyDiedEvent) event;

                EnemyAnimData data = enemyAnimMap.get(diedEvent.enemyId);

                if (data != null) {
                    deadBodies.add(new DeadBody(
                            data.lastX,
                            data.lastY,
                            data.lastAngle,
                            data.enemyType
                    ));

                    enemyAnimMap.remove(diedEvent.enemyId);
                }
            }

            if (event instanceof PlayerDiedEvent) {
                if (playerCorpse == null && gameStateView != null) {
                    Vec2 pPos = gameStateView.getPlayerPosition();
                    playerCorpse = new DeadBody(
                            pPos.x,
                            pPos.y,
                            gameStateView.getPlayerFacingAngle(),
                            "1"
                    );
                }
            }

            if (event instanceof MeleeAttackEvent) {
                MeleeAttackEvent meleeEvent = (MeleeAttackEvent) event;
                Enemy attacker = gameController.getEnemies().stream()
                        .filter(Enemy::isAlive)
                        .min((e1, e2) -> {
                            float d1 = (e1.getX() - meleeEvent.attackerX) * (e1.getX() - meleeEvent.attackerX) +
                                    (e1.getY() - meleeEvent.attackerY) * (e1.getY() - meleeEvent.attackerY);
                            float d2 = (e2.getX() - meleeEvent.attackerX) * (e2.getX() - meleeEvent.attackerX) +
                                    (e2.getY() - meleeEvent.attackerY) * (e2.getY() - meleeEvent.attackerY);
                            return Float.compare(d1, d2);
                        })
                        .orElse(null);

                if (attacker != null) {
                    float distSq = (attacker.getX() - meleeEvent.attackerX) * (attacker.getX() - meleeEvent.attackerX) +
                            (attacker.getY() - meleeEvent.attackerY) * (attacker.getY() - meleeEvent.attackerY);

                    if (distSq < 2500f) {
                        EnemyAnimData data = enemyAnimMap.computeIfAbsent(attacker.getEnemyId(), k -> new EnemyAnimData());
                        data.currentState = AnimationState.ATTACK;
                        data.stateTime = 0f;
                    }
                }
            }
        }

        for (Enemy enemy : gameController.getEnemies()) {
            EnemyAnimData data = enemyAnimMap.computeIfAbsent(enemy.getEnemyId(), k -> new EnemyAnimData());

            boolean moved = Math.abs(enemy.getX() - data.lastX) > 0.01f ||
                    Math.abs(enemy.getY() - data.lastY) > 0.01f;
            data.lastX = enemy.getX();
            data.lastY = enemy.getY();
            data.lastAngle = enemy.getFacingAngle();
            data.enemyType = enemy.getProfile().getEnemyTypeId();
            data.isMoving = moved;

            String enemyType = enemy.getProfile().getEnemyTypeId();
            String weaponId = enemy.getCurrentWeapon().getDefinition().getId();
            data.enemyType = enemyType + "_" + weaponId;
            EnemyAnimationSet animSet = assetLoader.getAnimationSet(enemyType, weaponId);

            AnimationState newState = data.currentState;

            if (data.currentState == AnimationState.ATTACK) {
                if (animSet.isAnimationFinished(AnimationState.ATTACK, data.stateTime)) {
                    newState = moved ? AnimationState.WALK : AnimationState.IDLE;
                }
            } else {
                newState = moved ? AnimationState.WALK : AnimationState.IDLE;
            }

            if (data.currentState != newState) {
                data.currentState = newState;
                data.stateTime = 0f;
            }

            if (data.currentState == AnimationState.ATTACK || data.currentState == AnimationState.WALK) {
                data.stateTime += dt;
            } else {
                data.stateTime = 0f;
            }
        }

        // Двері
        for (Door door : gameController.getDoors()) {
            float progress = doorAnimMap.getOrDefault(door, 0f);
            float animSpeed = 5f;

            if (door.getState() == DoorState.OPEN) {
                progress = Math.min(1f, progress + Gdx.graphics.getDeltaTime() * animSpeed);
            } else {
                progress = Math.max(0f, progress - Gdx.graphics.getDeltaTime() * animSpeed);
            }

            doorAnimMap.put(door, progress);
        }

    }

    private void draw() {
        ScreenUtils.clear(Color.BLUE);

        if (gameStateView != null) {
            float lerp = 10f * Gdx.graphics.getDeltaTime();
            Vec2 playerPos = gameStateView.getPlayerPosition();

            camera.position.x += (playerPos.x - camera.position.x) * lerp;
            camera.position.y += (playerPos.y - camera.position.y) * lerp;
        }

        float exactCameraX = camera.position.x;
        float exactCameraY = camera.position.y;
        camera.position.x = Math.round(exactCameraX);
        camera.position.y = Math.round(exactCameraY);

        camera.update();
        renderer.setView(camera);
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        renderer.render();

        if (gameStateView != null) {
            Vec2 playerPos = gameStateView.getPlayerPosition();

            spriteBatch.begin();
            for (DeadBody body : deadBodies) {
                EnemyAnimationSet animSet = assetLoader.getAnimationSet(body.enemyType);
                TextureRegion frame = animSet.getKeyFrame(AnimationState.DYING, body.stateTime, false);

                float width = 60f;
                float height = 40f;
                float originX = 9f;
                float originY = height / 2f;

                float drawX = body.x - originX;
                float drawY = body.y - originY;

                spriteBatch.draw(
                        frame,
                        drawX, drawY,
                        originX, originY,
                        width, height,
                        1f, 1f,
                        body.angle - 180
                );
            }

            if (playerCorpse != null) {
                float width = 80f;
                float height = 40f;
                float originX = 9f;
                float originY = height / 2f;
                float drawX = playerCorpse.x - originX;
                float drawY = playerCorpse.y - originY;

                spriteBatch.draw(
                        new TextureRegion(corpseTexture),
                        drawX, drawY,
                        originX, originY,
                        width, height,
                        1f, 1f,
                        playerCorpse.angle - 180
                );
            }
            spriteBatch.end();

            if (isPlayerMoving) {
                stateTime += Gdx.graphics.getDeltaTime();
            } else {
                stateTime = 0f;
            }

            TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);

            // Тимчасові текстури дверей
            spriteBatch.begin();
            for (Door door : gameController.getDoors()) {
                float progress = doorAnimMap.getOrDefault(door, 0f);

                float drawX = door.getX() - door.getWidth() / 2f;
                float drawY = door.getY() - door.getHeight() / 2f;

                float currentAngle = progress * 90f;

                if (door.getOrientation().equals("horizontal")) {
                    // Петля
                    float originX = 6f;
                    float originY = 6f;

                    spriteBatch.draw(
                            doorHorizontalTexture,
                            drawX, drawY,
                            originX, originY,
                            door.getWidth(), door.getHeight(),
                            1f, 1f,
                            currentAngle,  // кут повороту = додатний --- проти годинникової
                            0, 0,
                            doorHorizontalTexture.getWidth(), doorHorizontalTexture.getHeight(),
                            false, false
                    );

                } else if (door.getOrientation().equals("vertical")) {
                    // Петля
                    float originX = 6f;
                    float originY = 6f;

                    spriteBatch.draw(
                            doorVerticalTexture,
                            drawX, drawY,
                            originX, originY,
                            door.getWidth(), door.getHeight(),
                            1f, 1f,
                            -currentAngle,
                            0, 0,
                            doorVerticalTexture.getWidth(), doorVerticalTexture.getHeight(),
                            false, false
                    );
                }
            }
            spriteBatch.end();

            spriteBatch.begin();
            for (WeaponPickup pickup : gameController.getPickups()) {
                if (!pickup.canPick()) continue;
                float pickW = weaponPickupTexture.getWidth();
                float pickH = weaponPickupTexture.getHeight();

                float drawX = pickup.getX() - pickW / 2f;
                float drawY = pickup.getY() - pickH / 2f;

                spriteBatch.draw(
                        weaponPickupTexture,
                        drawX, drawY,
                        pickW, pickH
                );
            }
            spriteBatch.end();

            if(gameStateView != null && playerCorpse == null) {
                // Гравець
                float width = 45f;
                float height = 36f;
                float originX = 9f;
                float originY = height / 2f;
                float drawX = playerPos.x - originX;
                float drawY = playerPos.y - originY;
                float angle = gameStateView.getPlayerFacingAngle();

                spriteBatch.begin();

                spriteBatch.draw(
                        currentFrame,
                        drawX, drawY,
                        originX, originY,
                        width, height,
                        1f, 1f,
                        angle
                );
                spriteBatch.end();
            }

            // Прицілювання мишкою та лазер
            float dirX = mouseInWorld.x - playerPos.x;
            float dirY = mouseInWorld.y - playerPos.y;
            float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);

            if (distance > 0) {
                dirX /= distance;
                dirY /= distance;
            }

            float laserLength = 400f;
            float targetX = playerPos.x + dirX * laserLength;
            float targetY = playerPos.y + dirY * laserLength;

            // Вороги
            spriteBatch.begin();

            for (Enemy enemy : gameController.getEnemies()) {
                if (!enemy.isAlive()) continue;

                EnemyAnimData data = enemyAnimMap.getOrDefault(enemy.getEnemyId(), new EnemyAnimData());

                String enemyType = String.valueOf(enemy.getProfile().getEnemyTypeId());
                String weaponId = enemy.getCurrentWeapon().getDefinition().getId();

                EnemyAnimationSet animSet = assetLoader.getAnimationSet(enemyType, weaponId);
                boolean shouldLoop = (data.currentState != AnimationState.ATTACK);

                TextureRegion frame = animSet.getKeyFrame(data.currentState, data.stateTime, shouldLoop);

                float width = 45f;
                float height = 36f;
                float originX = 9f;
                float originY = height / 2f;

                float drawX = enemy.getX() - originX;
                float drawY = enemy.getY() - originY;
                float angle = enemy.getFacingAngle();

                spriteBatch.draw(
                        frame,
                        drawX, drawY,
                        originX, originY,
                        width, height,
                        1f, 1f,
                        angle
                );
            }

            spriteBatch.end();

            if (debugMode) {
                // Візуалізація зору ворогів
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.WHITE);
                if (gameController != null) {
                    for (org.core.entity.Enemy enemy : gameController.getEnemies()) {
                        if (!enemy.isAlive()) continue;

                        float baseAngle = enemy.getFacingAngle();
                        float fov = enemy.getCurrentFovAngle();
                        float viewRange = enemy.getProfile().getVisionRange();

                        float leftAngleRad = (float) Math.toRadians(baseAngle - fov / 2f);
                        float leftX = enemy.getX() + (float) Math.cos(leftAngleRad) * viewRange;
                        float leftY = enemy.getY() + (float) Math.sin(leftAngleRad) * viewRange;

                        float rightAngleRad = (float) Math.toRadians(baseAngle + fov / 2f);
                        float rightX = enemy.getX() + (float) Math.cos(rightAngleRad) * viewRange;
                        float rightY = enemy.getY() + (float) Math.sin(rightAngleRad) * viewRange;

                        shapeRenderer.line(enemy.getX(), enemy.getY(), leftX, leftY);
                        shapeRenderer.line(enemy.getX(), enemy.getY(), rightX, rightY);
                        shapeRenderer.arc(
                                enemy.getX(),
                                enemy.getY(),
                                viewRange,
                                baseAngle - fov / 2f,
                                fov
                        );
                    }
                }
                shapeRenderer.end();

                // Лазер гравця
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.line(playerPos.x, playerPos.y, targetX, targetY);
                shapeRenderer.end();

                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                for (VisualAttackEffect effect : attackEffects) {
                    shapeRenderer.setColor(effect.color);
                    shapeRenderer.line(effect.fromX, effect.fromY, effect.toX, effect.toY);
                }
                shapeRenderer.end();
            }
        }

        // Постріли
        spriteBatch.begin();
        for (BulletEffect effect : bulletEffects) {
            float bulletW = 15f;
            float bulletH = 15f;
            float originX = bulletW / 2f;
            float originY = bulletH / 2f;

            spriteBatch.draw(
                    bulletTexture,
                    effect.x - originX,
                    effect.y - originY,
                    originX, originY,
                    bulletW, bulletH,
                    1f, 1f,
                    effect.angle,
                    0, 0,
                    bulletTexture.getWidth(), bulletTexture.getHeight(),
                    false, false
            );
        }
        spriteBatch.end();

        // Знак оклику/питання над ворогами
        spriteBatch.begin();
        for (Enemy enemy : gameController.getEnemies()) {
            if (!enemy.isAlive()) continue;

            Texture statusTexture;
            switch (enemy.getCurrentState()) {
                case ATTACK ->
                        statusTexture = alertTexture;
                case SEARCH ->
                        statusTexture = searchTexture;
                default -> statusTexture = null;
            }

            if (statusTexture != null) {
                float markSize = 24f;

                float drawX = enemy.getX() - (markSize / 2f);
                float drawY = enemy.getY() + 20f;

                spriteBatch.draw(statusTexture, drawX, drawY, markSize, markSize);
            }
        }

        spriteBatch.end();
    }

    @Override
    public void dispose () {
        map.dispose();
        renderer.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
        doorHorizontalTexture.dispose();
        doorVerticalTexture.dispose();
        attackEffects.clear();
        playerSprite.dispose();
        alertEffects.clear();
        alertTexture.dispose();
        deadBodies.clear();
        corpseTexture.dispose();
        if (bulletTexture != null) bulletTexture.dispose();
        if (assetLoader != null) assetLoader.dispose();
        if (weaponPickupTexture != null) weaponPickupTexture.dispose();
        if (animationFrames != null) {
            for (Texture texture : animationFrames) {
                if (texture != null) texture.dispose();
            }
        }
    }

    private void restart() {
        deadBodies.clear();
        playerCorpse = null;

        LevelTmxLoader levelLoader = new LevelTmxLoader();
        LevelData levelData = levelLoader.parseMapObjects(map);
        gameController = new GameController(weaponRegistry, enemyProfileRegistry, new java.util.HashMap<>(), weaponSystem);
        gameController.loadLevel(levelData);
        this.gameStateView = gameController.getStateView();
    }

}