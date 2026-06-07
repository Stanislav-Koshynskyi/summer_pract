package org.core.entity;

import lombok.Getter;
import org.core.collision.PathBlocker;
import org.core.enums.DoorState;
import org.core.math.Rect;

@Getter
public class Door extends Entity implements PathBlocker {
    public static final float DEFAULT_OPENING_DURATION = 0.35f;
    public static final float DEFAULT_SOUND_ATTENUATION = 0.33f; // CLOSED блокує звук
    private static final float DEFAULT_PAUSE_DURATION = 1;

    private final String doorId;
    private DoorState state;
    private float openingTimer = 0;
    private float closingTimer = 0;
    private float pauseTimer = 0;
    private final float openingDuration;
    private final float soundAttenuationFactor;

    public Door(String doorId, float x, float y, float width, float height,
                DoorState initialState, float openingDuration, float soundAttenuationFactor) {
        super(x, y, width, height);
        this.doorId = doorId;
        this.state = initialState;
        this.openingDuration = openingDuration;
        this.soundAttenuationFactor = soundAttenuationFactor;
    }

    public Door(String doorId, float x, float y, float width, float height,
                DoorState initialState) {
        this(doorId, x, y, width, height, initialState,
                DEFAULT_OPENING_DURATION, DEFAULT_SOUND_ATTENUATION);
    }

    public void requestOpen() {
        if (state == DoorState.CLOSED && pauseTimer <= 0) {
            state = DoorState.OPENING;
            openingTimer = openingDuration;
        }
    }
    public void requestClose() {
        if (state == DoorState.OPEN && pauseTimer <= 0) {
            state = DoorState.CLOSING;
            closingTimer = openingDuration;
        }
    }

    public void update(float delta) {
        openingTimer -= delta;
        pauseTimer -= delta;
        closingTimer -= delta;
        if (state == DoorState.OPENING && openingTimer <= 0){
            state = DoorState.OPEN;
            pauseTimer = DEFAULT_PAUSE_DURATION;
        }
        if (state == DoorState.CLOSING && closingTimer <= 0){
            state = DoorState.CLOSED;
            pauseTimer = DEFAULT_PAUSE_DURATION;
        }
    }

    @Override
    public Rect getBounds() {
        return Rect.fromCenter(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean blocksMovement() {
        return state == DoorState.CLOSED || state == DoorState.OPENING || state == DoorState.CLOSING;
    }

    @Override
    public boolean blocksVision() {
        return state == DoorState.CLOSED;
    }

    @Override
    public boolean blocksProjectile() {
        return state == DoorState.CLOSED || state == DoorState.OPENING || state == DoorState.CLOSING;
    }

    @Override
    public boolean blocksSound() {
        return state == DoorState.CLOSED;
    }

    @Override
    public float getSoundAttenuationFactor() {
        return blocksSound() ? soundAttenuationFactor : 1.0f;
    }

    @Override
    public String toString() {
        return String.format("Door(id=%s, state=%s, pos=(%.0f,%.0f), size=(%.0f,%.0f))",
                doorId, state, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean isTraversable() {
        return state != DoorState.LOCKED;
    }

    @Override
    public float getPathCost() {
        switch (state) {
            case LOCKED -> {
                return Float.POSITIVE_INFINITY;
            }
            case CLOSED, OPENING -> {
                return 5f;
            }
            case CLOSING -> {
                return 8f;
            }
            case null, default -> {
                return 1f;
            }
        }
    }
}
