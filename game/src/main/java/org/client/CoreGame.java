package org.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import lombok.Setter;
import org.content.weapon_behavior.SimpleRayCastBehavior;
import org.core.data.*;
import org.core.entity.*;
import org.core.enums.WeaponType;
import org.core.state.*;
import org.core.weapon.Weapon;
import org.core.weapon.WeaponDefinition;

import java.util.ArrayList;
import java.util.List;

public class CoreGame extends ApplicationAdapter {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private CameraInputController cameraController;
    private SpriteBatch spriteBatch;
    @Setter
    private GameStateView gameStateView;
    private ShapeRenderer shapeRenderer;
    private LevelState levelState;

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        map = new TmxMapLoader().load("maps/tests/test.tmx");
        float unitScale = 1f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1600, 900);
        camera.update();

        //cameraController = new CameraInputController(camera);
        //Gdx.input.setInputProcessor(cameraController);

        LevelTmxLoader levelLoader = new LevelTmxLoader();
        LevelData levelData = levelLoader.parseMapObjects(map);
        levelState = new LevelState();
        Weapon weapon = new Weapon(
                new WeaponDefinition(
                        "test",
                        WeaponType.HITSCAN,
                        1,
                        100,
                        1,
                        10,
                        false,
                        false,
                        new SimpleRayCastBehavior()
                )
        );

        Player player = new Player(levelData.playerSpawn.x, levelData.playerSpawn.y, 20f, 20f, weapon);

        List<Enemy> enemies = new ArrayList<>();
        List<Door> doors = new ArrayList<>();
        List<WeaponPickup> pickups = new ArrayList<>();

        levelState.reset(
                levelData.worldGeometry,
                player,
                enemies,
                doors,
                pickups,
                levelData.goalType,
                levelData.targetEnemyId
        );

        this.gameStateView = new GameStateView(levelState);

        shapeRenderer = new ShapeRenderer();
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
        // Placeholder
    }

    private void draw() {
        ScreenUtils.clear(Color.BLUE);

        camera.update();
        renderer.setView(camera);
        renderer.render();

        if (gameStateView != null) {
            org.core.math.Vec2 playerPos = gameStateView.getPlayerPosition();

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLACK);

            shapeRenderer.circle(playerPos.x, playerPos.y, 16f);

            shapeRenderer.end();
        }
    }

    @Override
    public void dispose () {
        map.dispose();
        renderer.dispose();
        shapeRenderer.dispose();
    }

}