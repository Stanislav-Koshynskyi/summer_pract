package org.core.event;

import org.core.enums.MovementMode;

public class FootstepEvent extends SoundEvent {
    public MovementMode movementMode;
    public boolean isPlayer;

    public FootstepEvent(float x, float y, MovementMode movementMode, boolean isPlayer, float radius) {
        super(x, y, radius);
        this.movementMode = movementMode;
        this.isPlayer = isPlayer;
    }

    @Override
    public SoundType getSoundType() {
        return movementMode == MovementMode.RUN ? SoundType.FOOTSTEP_RUN : SoundType.FOOTSTEP_WALK;
    }

    @Override
    public float getRadius() {
        return radius;
    }
}

