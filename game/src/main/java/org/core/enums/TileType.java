package org.core.enums;

import lombok.Getter;

public enum TileType {
    FLOOR(true, false, false, 1.0f, 1f),
    WALL(false, true, true, 0.33f, Float.MAX_VALUE),
    LOW_OBSTACLE(true, false, false, 1.0f, 1f),
    VENT(true, true, true, 1.0f, 1f);

    private final boolean canPass;
    private final boolean blocksVision;
    private final boolean blocksProjectile;
    @Getter
    private final float soundAttenuationFactor;
    @Getter
    private final float pathCost;

    TileType(boolean canPass, boolean blocksVision, boolean blocksProjectile, float soundAttenuation
    , float pathCost) {
        this.canPass = canPass;
        this.blocksVision = blocksVision;
        this.blocksProjectile = blocksProjectile;
        this.soundAttenuationFactor = soundAttenuation;
        this.pathCost = pathCost;
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


