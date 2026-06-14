package org.core.enums;

import lombok.Getter;
@Getter
public enum MovementMode {
    WALK(1.2f, 100), RUN(2.2f, 250), SNEAK(1f, 0);
    private final float speedMultiply;
    private final float sound;
    MovementMode(float speedMultiply, float sound){
        this.speedMultiply = speedMultiply;
        this.sound = sound;
    }

}