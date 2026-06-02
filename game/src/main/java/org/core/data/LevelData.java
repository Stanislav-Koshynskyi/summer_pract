package org.core.data;

import org.core.geometry.WorldGeometry;
import java.util.List;

/**
 * Complete level information loaded from Tiled .tmx file via LevelTmxLoader.
 */
public class LevelData {
    public WorldGeometry worldGeometry;

    public PlayerSpawnData playerSpawn;
    public List<EnemySpawnData> enemySpawns;
    public List<DoorData> doors;
    public List<WaypointData> waypoints;
    public List<WeaponPickupData> weaponPickups;
    public List<ExitData> exits;

    public String goalType;
    public String targetEnemyId;

    public String levelId;
    public String levelName;

    public LevelData(WorldGeometry worldGeometry,
                    PlayerSpawnData playerSpawn,
                    List<EnemySpawnData> enemySpawns,
                    List<DoorData> doors,
                    List<WaypointData> waypoints,
                    List<WeaponPickupData> weaponPickups,
                    List<ExitData> exits,
                    String goalType,
                    String targetEnemyId) {
        this.worldGeometry = worldGeometry;
        this.playerSpawn = playerSpawn;
        this.enemySpawns = enemySpawns;
        this.doors = doors;
        this.waypoints = waypoints;
        this.weaponPickups = weaponPickups;
        this.exits = exits;
        this.goalType = goalType;
        this.targetEnemyId = targetEnemyId;
    }

    @Override
    public String toString() {
        return String.format("LevelData(goal=%s, enemies=%d, doors=%d, waypoints=%d)",
                           goalType, enemySpawns.size(), doors.size(), waypoints.size());
    }
}


