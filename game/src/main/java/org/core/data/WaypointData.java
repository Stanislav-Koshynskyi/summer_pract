package org.core.data;

public class WaypointData {
    public String pathId;
    public int order;
    public float x, y;

    public WaypointData(String pathId, int order, float x, float y) {
        this.pathId = pathId;
        this.order = order;
        this.x = x;
        this.y = y;
    }
}


