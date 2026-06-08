package org.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import lombok.Setter;
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

public class CoreGame extends ApplicationAdapter {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private CameraInputController cameraController;
    private SpriteBatch spriteBatch;
    private Texture doorOpenedTexture;
    private Texture doorClosedTexture;
    @Setter
    private GameStateView gameStateView;
    private ShapeRenderer shapeRenderer;
    private GameController gameController;
    private EnemyProfile testEnemyProfile;
    private WeaponSystem weaponSystem;
    private boolean debugMode = false;
    private final Vector3 mouseInWorld = new Vector3();
    private final List<VisualAttackEffect> attackEffects = new ArrayList<>();

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        map = new TmxMapLoader().load("maps/level_01.tmx");
        float unitScale = 1f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1600, 900);

        //cameraController = new CameraInputController(camera);
        //Gdx.input.setInputProcessor(cameraController);

        // тестовий профіль, щоб програма не падала
        testEnemyProfile = new EnemyProfile(
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
                23f,
                600
        );

        weaponSystem = new WeaponSystem();

        LevelTmxLoader levelLoader = new LevelTmxLoader();
        LevelData levelData = levelLoader.parseMapObjects(map);

        gameController = new GameController(null, testEnemyProfile, new java.util.HashMap<>(), weaponSystem);
        gameController.loadLevel(levelData);

        this.gameStateView = gameController.getStateView();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        doorOpenedTexture = new Texture(Gdx.files.internal("textures/door_opened.png"));
        doorClosedTexture = new Texture(Gdx.files.internal("textures/door_closed.png"));
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            debugMode = !debugMode;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            gameController.shoot();
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

        List<GameEvent> events = gameController.drainEvents();
        for (GameEvent event : events) {
            if (event instanceof ShotFiredEvent) {
                ShotFiredEvent shot = (ShotFiredEvent) event;

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
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLUE);

        camera.position.x = Math.round(camera.position.x);
        camera.position.y = Math.round(camera.position.y);
        camera.update();
        renderer.setView(camera);
        renderer.render();

        if (gameStateView != null) {
            Vec2 playerPos = gameStateView.getPlayerPosition();

            // Камера
            camera.position.set(playerPos.x, playerPos.y, 0);

            // Гравець
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(playerPos.x, playerPos.y, 16f);
            shapeRenderer.end();

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

            // Вороги (різні стани)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            if (gameController != null && gameController.getEnemies() != null) {
                for (Enemy enemy : gameController.getEnemies()) {
                    switch (enemy.getCurrentState()) {
                        case ATTACK -> shapeRenderer.setColor(Color.RED);
                        case SEARCH -> shapeRenderer.setColor(Color.YELLOW);
                        case INVESTIGATE -> shapeRenderer.setColor(Color.GREEN);
                        case PATROL -> shapeRenderer.setColor(Color.BLUE);
                        case SUSPICIOUS -> shapeRenderer.setColor(Color.PURPLE);
                        default -> shapeRenderer.setColor(Color.BLACK);
                    }
                    shapeRenderer.circle(enemy.getX(), enemy.getY(), 16f);
                }
            }
            shapeRenderer.end();

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
            }
        }

        // Постріли
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (VisualAttackEffect effect : attackEffects) {
            shapeRenderer.setColor(effect.color);
            shapeRenderer.line(effect.fromX, effect.fromY, effect.toX, effect.toY);
        }
        shapeRenderer.end();

        // Тимчасові текстури дверей
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        for (Door door : gameController.getDoors()) {
            float drawX = door.getX() - door.getWidth() / 2f;
            float drawY = door.getY() - door.getHeight() / 2f;
            if (door.getState() == DoorState.OPEN) {
                spriteBatch.draw(doorOpenedTexture, drawX, drawY, door.getWidth(), door.getHeight());
            }
            if (door.getState() == DoorState.CLOSED) {
                spriteBatch.draw(doorClosedTexture, drawX, drawY, door.getWidth(), door.getHeight());
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
        doorClosedTexture.dispose();
        doorOpenedTexture.dispose();
        attackEffects.clear();
    }

    private void restart() {
        LevelTmxLoader levelLoader = new LevelTmxLoader();
        LevelData levelData = levelLoader.parseMapObjects(map);
        gameController = new GameController(null, testEnemyProfile, new java.util.HashMap<>(), weaponSystem);
        gameController.loadLevel(levelData);
        this.gameStateView = gameController.getStateView();
    }

}