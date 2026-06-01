package org.core.event;

import org.core.enums.AIState;

public class EnemyAlertedEvent extends GameEvent {
    public String enemyId;
    public float x, y;
    public AIState newState;

    public EnemyAlertedEvent(String enemyId, float x, float y, AIState newState) {
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.newState = newState;
    }

    @Override
    public String getEventType() {
        return "EnemyAlerted";
    }
}


