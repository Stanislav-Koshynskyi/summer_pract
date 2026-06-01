package org.core.entity;

import lombok.Getter;
import lombok.Setter;
import org.core.enums.DoorState;
import org.core.math.Rect;

@Getter
@Setter
public class Door extends Entity {
    private final String doorId;
    private DoorState state;
    private float openingTimer;
    private final float openingDuration;

    public Door(String doorId, float x, float y, float width, float height,
                DoorState initialState, float openingDuration) {
        super(x, y, width, height);
        this.doorId = doorId;
        this.state = initialState;
        this.openingDuration = openingDuration;
        this.openingTimer = 0f;
    }

    public void requestOpen() {
        if (state == DoorState.CLOSED) {
            state = DoorState.OPENING;
            openingTimer = openingDuration;
        }
    }

    public void update(float delta) {
        if (state == DoorState.OPENING) {
            openingTimer -= delta;
            if (openingTimer <= 0f) {
                state = DoorState.OPEN;
                openingTimer = 0f;
            }
        }
    }

    public boolean blocksMovement() {
        return state == DoorState.CLOSED || state == DoorState.OPENING;
    }

    public boolean blocksProjectiles() {
        return state == DoorState.CLOSED || state == DoorState.OPENING;
    }

    public boolean isVisionBlocking() {
        return state == DoorState.CLOSED;
    }

    @Override
    public String toString() {
        return String.format("Door(id=%s, state=%s, pos=(%.0f,%.0f), size=(%.0f,%.0f))",
                           doorId, state, getX(), getY(), getWidth(), getHeight());
    }
}


