package org.core.state;

import lombok.Getter;
import lombok.Setter;
import org.core.data.ExitData;
import org.core.entity.Door;
import org.core.entity.Enemy;
import org.core.entity.Player;
import org.core.entity.WeaponPickup;
import org.core.enums.GamePhase;
import org.core.enums.GoalType;
import org.core.event.EnemyDiedEvent;
import org.core.event.GameEvent;
import org.core.event.SoundEventQueue;
import org.core.geometry.WorldGeometry;
import org.core.math.Rect;

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
    @Getter
    private final List<Rect> exitBounds = new ArrayList<>();


    public void reset(WorldGeometry geometry, Player player,
                      List<Enemy> enemies,
                      List<Door> doors,
                      List<WeaponPickup> pickups,
                      List<ExitData> exitData,
                      GoalType goalType,
                      String targetEnemyId) {
        this.worldGeometry = geometry;
        this.player = player;
        this.goalType = goalType;
        this.targetEnemyId = targetEnemyId;

        this.exitBounds.clear();
        for (ExitData exit : exitData) {
            exitBounds.add(Rect.fromCenter(exit.x, exit.y, exit.width, exit.height));
        }


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

    public List<GameEvent> flushDeadEnemies() {
        Iterator<Enemy> it = enemies.iterator();
        List<GameEvent> deadEvents = new ArrayList<>();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (!e.isAlive()) {
                deadEvents.add(new EnemyDiedEvent(e.getX(), e.getY(), e.getEnemyId()));
                corpses.add(e);
                it.remove();
            }
        }
        return deadEvents;
    }

    public List<GameEvent> getGameEvents() {
        return new ArrayList<>(gameEvents);
    }
    public void addGameEvent(GameEvent event) {
        gameEvents.add(event);
    }
    public void addAllGameEvent(List<GameEvent> events){
        gameEvents.addAll(events);
    }
    public void clearGameEvents() {
        gameEvents.clear();
    }
    public boolean isPlayerAtExit() {
        if (player == null) return false;
        Rect playerBounds = Rect.fromCenter(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        for (Rect exitBound : exitBounds) {
            if (playerBounds.overlaps(exitBound)) {
                return true;
            }
        }
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
