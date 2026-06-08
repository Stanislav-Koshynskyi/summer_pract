package org.core.enums;

import lombok.Getter;

public enum MovementMode {
    WALK(1), RUN(2), SNEAK(0.5f);
    @Getter
    private final float speedMultiply;
    MovementMode(float speedMultiply){
        this.speedMultiply = speedMultiply;
    }
}