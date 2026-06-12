package org.core.data;

import org.core.enums.DoorState;

public class DoorData {
    public String doorId;
    public float x, y;
    public float width, height;
    public DoorState initialState;
    public int[][] blockedTiles;
    public float openingDuration;
    public String orientation;

    public DoorData(String doorId, float x, float y, float width, float height,
                    DoorState initialState, int[][] blockedTiles, float openingDuration, String orientation) {
        this.doorId = doorId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.initialState = initialState;
        this.blockedTiles = blockedTiles;
        this.openingDuration = openingDuration;
        this.orientation = orientation;
    }
}


