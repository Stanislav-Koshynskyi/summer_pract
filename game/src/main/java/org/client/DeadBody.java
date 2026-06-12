package org.client;

public class DeadBody {
    public float x, y, angle;
    public String enemyType;
    public float stateTime = 0f;

    public DeadBody(float x, float y, float angle, String enemyType) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.enemyType = enemyType;
    }
}
