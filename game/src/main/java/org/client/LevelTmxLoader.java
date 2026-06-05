package org.client;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.core.data.*;
import org.core.enums.TileType;
import org.core.geometry.WorldGeometry;

import java.util.*;

public class LevelTmxLoader {
    public LevelData parseMapObjects(TiledMap map) {

        MapProperties mapProperties = map.getProperties();
        int mapHeightInTiles = mapProperties.get("height", Integer.class);
        int mapWidthInTiles = mapProperties.get("width", Integer.class);
        int tileSize = mapProperties.get("tileheight", Integer.class);
        float mapHeightInPixels = mapHeightInTiles * tileSize;
        org.core.enums.GoalType goalType = org.core.enums.GoalType.ESCAPE;

        PlayerSpawnData playerSpawn = null;
        List<EnemySpawnData> enemySpawns = new ArrayList<>();
        List<DoorData> doors = new ArrayList<>();
        List<WaypointData> waypoints = new ArrayList<>();
        List<WeaponPickupData> weaponPickups = new ArrayList<>();
        List<ExitData> exits = new ArrayList<>();
        TileType[][] tiles = new TileType[mapHeightInTiles][mapWidthInTiles];

        // Стіни
        TiledMapTileLayer wallsLayer = (TiledMapTileLayer) map.getLayers().get("walls");

        for (int y = 0; y < mapHeightInTiles; y++) {
            for (int x = 0; x < mapWidthInTiles; x++) {
                TileType type = TileType.FLOOR;

                if (wallsLayer != null && wallsLayer.getCell(x, y) != null && wallsLayer.getCell(x, y).getTile() != null) {
                    type = TileType.WALL;
                }

                tiles[y][x] = type;
            }
        }
        WorldGeometry worldGeometry = new WorldGeometry(tiles, tileSize);

        // Ціль
        if (mapProperties.containsKey("goalType")) {
            String goalStr = mapProperties.get("goalType", String.class);
            try {
                goalType = org.core.enums.GoalType.valueOf(goalStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                System.err.println("goalType error");
            }
        }

        // Противник
        String targetEnemyId = mapProperties.get("targetEnemyId", String.class);
        if (targetEnemyId == null) {
            System.err.println("No target!");
        }

        // Об'єкти
        MapLayer objectsLayer = map.getLayers().get("objects");
        if (objectsLayer != null) {
            for (MapObject object : objectsLayer.getObjects()) {
                MapProperties objectProps = object.getProperties();

                float tiledX = objectProps.get("x", Float.class);
                float tiledY = objectProps.get("y", Float.class);

                float width = objectProps.containsKey("width") ? objectProps.get("width", Float.class) : 0f;
                float height = objectProps.containsKey("height") ? objectProps.get("height", Float.class) : 0f;

                float worldX = tiledX;
                float worldY = tiledY;

                String objectType = objectProps.get("type", String.class);
                if (objectType == null) {
                    continue;
                }

                if (objectType.equals("PlayerSpawn")) {
                    playerSpawn = new PlayerSpawnData(worldX, worldY);
                }

                if (objectType.equals("EnemySpawn")) {
                    int enemyTypeId = objectProps.get("enemyType", Integer.class);
                    int weaponId = objectProps.get("weaponId", Integer.class);
                    int patrolPathId = objectProps.get("patrolPath", Integer.class);
                    String facingAngle =  objectProps.get("facingAngle", String.class);
                    float facingAngleDegrees = convertToAngle(facingAngle);
                    int enemyId = 1; //Тимчасово

                    String enemyType = String.valueOf(enemyTypeId);
                    String weapon = String.valueOf(weaponId);
                    String patrolPath = String.valueOf(patrolPathId);
                    String enemy = String.valueOf(enemyId);

                    EnemySpawnData enemyData = new EnemySpawnData(
                            worldX, worldY, enemyType, weapon, patrolPath, facingAngleDegrees, enemy
                    );
                    enemySpawns.add(enemyData);
                }

                if (objectType.equals("Door")) {
                    String doorId = objectProps.get("doorId", String.class);
                    String orientation = objectProps.get("orientation", String.class);
                    float widthDoor = objectProps.containsKey("width") ? objectProps.get("width", Float.class) : (float) tileSize;
                    float heightDoor = objectProps.containsKey("height") ? objectProps.get("height", Float.class) : (float) tileSize;
                    float openingDuration = objectProps.containsKey("openingDuration") ? objectProps.get("openingDuration", Float.class) : 1.0f;
                    int startTileX = (int) (worldX / tileSize);
                    int startTileY = (int) (worldY / tileSize);

                    int tilesX = Math.max(1, (int) Math.ceil(width / tileSize));
                    int tilesY = Math.max(1, (int) Math.ceil(height / tileSize));

                    int[][] blockedTiles = new int[tilesX * tilesY][2];
                    int index = 0;

                    for (int tx = 0; tx < tilesX; tx++) {
                        for (int ty = 0; ty < tilesY; ty++) {
                            blockedTiles[index][0] = startTileX + tx;
                            blockedTiles[index][1] = startTileY + ty;
                            index++;
                        }
                    }

                    org.core.enums.DoorState initialState = org.core.enums.DoorState.CLOSED;
                    if (objectProps.containsKey("initialState")) {
                        try {
                            initialState = org.core.enums.DoorState.valueOf(objectProps.get("initialState", String.class).toUpperCase());
                        } catch (IllegalArgumentException e) {
                            initialState = org.core.enums.DoorState.CLOSED;
                        }
                    }
                    DoorData doorData = new DoorData(
                            doorId, worldX, worldY, widthDoor, heightDoor, initialState, blockedTiles, openingDuration
                    );
                    doors.add(doorData);
                }

                if (objectType.equals("WeaponPickup")) {
                    int weaponId = objectProps.get("weaponId", Integer.class);
                    String weapon = String.valueOf(weaponId);

                    WeaponPickupData weaponPickupData = new WeaponPickupData(
                            weapon, worldX, worldY
                    );
                    weaponPickups.add(weaponPickupData);
                }

                if (objectType.equals("Exit")) {
                    int exitId = objectProps.get("exitId", Integer.class);
                    float widthExit = objectProps.containsKey("width") ? objectProps.get("width", Float.class) : (float) tileSize;
                    float heightExit = objectProps.containsKey("height") ? objectProps.get("height", Float.class) : (float) tileSize;

                    String exit = String.valueOf(exitId);
                    ExitData exitData = new ExitData(
                            exit, worldX, worldY, widthExit, heightExit
                    );

                    exits.add(exitData);
                }

                if (objectType.equals("Waypoint")) {
                    int pathId = objectProps.get("pathId", Integer.class);
                    int order = objectProps.get("order", Integer.class);

                    String path = String.valueOf(pathId);

                    WaypointData waypointData = new WaypointData(
                            path, order, worldX, worldY
                    );
                    waypoints.add(waypointData);
                }
            }
        }
        return new LevelData(
                worldGeometry,
                playerSpawn,
                enemySpawns,
                doors,
                waypoints,
                weaponPickups,
                exits,
                goalType,
                targetEnemyId
        );
    }

    private float convertToAngle(String stringAngle) {
        if ("down".equals(stringAngle)) return 90f;
        if ("left".equals(stringAngle)) return 180f;
        if ("up".equals(stringAngle)) return 270f;
        return 0f; // right
    }
}