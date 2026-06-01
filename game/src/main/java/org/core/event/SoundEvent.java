package org.core.event;

public abstract class SoundEvent {
    public float x, y;
    public float radius;

    public SoundEvent(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public abstract SoundType getSoundType();
    public abstract float getRadius();

    public enum SoundType {
        GUNSHOT,
        SUPPRESSED_SHOT,
        FOOTSTEP_RUN,
        FOOTSTEP_WALK,
        BODY_FALL
    }
}

