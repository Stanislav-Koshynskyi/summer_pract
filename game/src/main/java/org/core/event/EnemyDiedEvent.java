package org.core.event;

public class EnemyDiedEvent extends GameEvent {
    public float x, y;
    public String enemyId;

    public EnemyDiedEvent(float x, float y, String enemyId) {
        this.x = x;
        this.y = y;
        this.enemyId = enemyId;
    }

    @Override
    public String getEventType() {
        return "EnemyDied";
    }
}


