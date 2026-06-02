package org.core.collision;

import org.core.math.Rect;

public interface Blocker {
    Rect getBounds();
    boolean blocksMovement();
    boolean blocksVision();
    boolean blocksProjectile();
    boolean blocksSound();
    float getSoundAttenuationFactor();
}