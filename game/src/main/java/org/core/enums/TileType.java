package org.core.enums;

import lombok.Getter;

public enum TileType {
    FLOOR(true, false, false, 1.0f),
    WALL(false, true, true, 0.33f),
    LOW_OBSTACLE(true, false, false, 1.0f),
    VENT(true, true, true, 1.0f);

    private final boolean canPass;
    private final boolean blocksVision;
    private final boolean blocksProjectile;
    @Getter
    private final float soundAttenuationFactor;

    TileType(boolean canPass, boolean blocksVision, boolean blocksProjectile, float soundAttenuation) {
        this.canPass = canPass;
        this.blocksVision = blocksVision;
        this.blocksProjectile = blocksProjectile;
        this.soundAttenuationFactor = soundAttenuation;
    }

    public boolean canPass() {
        return canPass;
    }

    public boolean blocksVision() {
        return blocksVision;
    }

    public boolean blocksProjectile() {
        return blocksProjectile;
    }

    public boolean attenuatesSound() {
        return soundAttenuationFactor < 1.0f;
    }
}


