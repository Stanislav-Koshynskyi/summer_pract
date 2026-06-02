package org.core.entity;

import lombok.Getter;
import org.core.collision.Blocker;
import org.core.enums.DoorState;
import org.core.math.Rect;

@Getter
public class Door extends Entity implements Blocker {
    public static final float DEFAULT_OPENING_DURATION      = 0.35f;
    public static final float DEFAULT_SOUND_ATTENUATION     = 0.33f; // CLOSED блокує звук

    private final String doorId;
    private DoorState state;
    private float openingTimer;
    private final float openingDuration;
    private final float soundAttenuationFactor;
    public Door(String doorId, float x, float y, float width, float height,
                DoorState initialState, float openingDuration, float soundAttenuationFactor) {
        super(x, y, width, height);
        this.doorId                = doorId;
        this.state                 = initialState;
        this.openingDuration       = openingDuration;
        this.soundAttenuationFactor = soundAttenuationFactor;
        this.openingTimer          = 0f;
    }
    public Door(String doorId, float x, float y, float width, float height,
                DoorState initialState) {
        this(doorId, x, y, width, height, initialState,
                DEFAULT_OPENING_DURATION, DEFAULT_SOUND_ATTENUATION);
    }
    public void requestOpen() {
        if (state == DoorState.CLOSED) {
            state         = DoorState.OPENING;
            openingTimer  = openingDuration;
        }
    }
    public void update(float delta) {
        if (state == DoorState.OPENING) {
            openingTimer -= delta;
            if (openingTimer <= 0f) {
                openingTimer = 0f;
                state        = DoorState.OPEN;
            }
        }
    }
    @Override
    public Rect getBounds() {
        return Rect.fromCenter(getX(), getY(), getWidth(), getHeight());
    }
    @Override
    public boolean blocksMovement() {
        return state == DoorState.CLOSED || state == DoorState.OPENING;
    }
    @Override
    public boolean blocksVision() {
        return state == DoorState.CLOSED;
    }
    @Override
    public boolean blocksProjectile() {
        return state == DoorState.CLOSED || state == DoorState.OPENING;
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
}
