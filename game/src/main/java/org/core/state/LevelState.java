package org.core.state;

import lombok.Getter;
import lombok.Setter;
import org.core.entity.Door;
import org.core.entity.Enemy;
import org.core.entity.Player;
import org.core.entity.WeaponPickup;
import org.core.enums.GamePhase;
import org.core.enums.GoalType;
import org.core.event.GameEvent;
import org.core.event.SoundEventQueue;
import org.core.geometry.WorldGeometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LevelState {

    @Getter
    private WorldGeometry worldGeometry;
    @Getter
    private Player player;

    @Getter
    private final List<Enemy> enemies = new ArrayList<>();
    @Getter
    private final List<Enemy> corpses = new ArrayList<>();

    @Getter
    private final List<Door> doors = new ArrayList<>();
    @Getter
    private final List<WeaponPickup> pickups = new ArrayList<>();

    @Getter
    @Setter
    private GamePhase phase = GamePhase.PLAYING;

    @Getter
    private final LevelStats stats = new LevelStats();
    private final List<GameEvent> gameEvents = new ArrayList<>();
    @Getter
    private final SoundEventQueue soundEventQueue = new SoundEventQueue();
    @Getter
    private GoalType goalType;
    @Getter
    private String targetEnemyId;


    public void reset(WorldGeometry geometry, Player player,
                      List<Enemy> enemies,
                      List<Door> doors,
                      List<WeaponPickup> pickups,
                      GoalType goalType,
                      String targetEnemyId) {
        this.worldGeometry = geometry;
        this.player = player;
        this.goalType = goalType;
        this.targetEnemyId = targetEnemyId;

        this.enemies.clear();
        this.enemies.addAll(enemies);

        this.corpses.clear();
        this.doors.clear();
        this.doors.addAll(doors);
        this.pickups.clear();
        this.pickups.addAll(pickups);

        this.phase = GamePhase.PLAYING;
        this.stats.reset();
        this.gameEvents.clear();
        this.soundEventQueue.clear();
    }

    public boolean isPlaying() {
        return phase == GamePhase.PLAYING;
    }

    public void flushDeadEnemies() {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isAlive()) {
                corpses.add(e);
                it.remove();
            }
        }
    }

    public List<GameEvent> getGameEvents() {
        return new ArrayList<>(gameEvents);
    }
    public void addGameEvent(GameEvent event) {
        gameEvents.add(event);
    }
    public void clearGameEvents() {
        gameEvents.clear();
    }
    public boolean isPlayerAtExit() {
        if (player == null || worldGeometry == null) return false;
        return false;
    }
    public boolean isEnemyAlive(String enemyId) {
        if (enemyId == null) return false;
        for (Enemy e : enemies) {
            if (enemyId.equals(e.getEnemyId()) && e.isAlive()) return true;
        }
        return false;
    }
    public boolean areAllEnemiesDead() {
        return enemies.isEmpty();
    }
    public Door getDoorById(String doorId) {
        for (Door door : doors) {
            if (door.getDoorId().equals(doorId)) {
                return door;
            }
        }
        return null;
    }
    public void removeWeaponPickup(WeaponPickup pickup) {
        pickups.remove(pickup);
    }
    public WeaponPickup getNearestWeaponPickup(float x, float y, float radius) {
        WeaponPickup nearest = null;
        float minDist = radius;
        for (WeaponPickup pickup : pickups) {
            if (pickup.isEmpty()) continue;
            float dx = pickup.getX() - x;
            float dy = pickup.getY() - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < minDist) {
                minDist = dist;
                nearest = pickup;
            }
        }
        return nearest;
    }
}
