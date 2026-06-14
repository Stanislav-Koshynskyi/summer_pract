package org.core.data;

public class ExitData {
    public String exitId;
    public float x, y;
    public float width, height;

    public ExitData(String exitId, float x, float y, float width, float height) {
        this.exitId = exitId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.x = x + width / 2;
        this.y = y + height / 2;
    }
}


