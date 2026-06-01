package org.core.event;

public class PlayerDiedEvent extends GameEvent {
    public float x, y;

    public PlayerDiedEvent(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String getEventType() {
        return "PlayerDied";
    }
}


