package org.core.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Entity {
    private float x, y;
    private final float width, height; // hitbox size
    @Setter
    private float velocityX;
    @Setter
    private float velocityY;
    public Entity(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void move(float dx, float dy) {
        x += dx;
        y += dy;
    }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void setVelocity(float vx,float vy){
        velocityX = vx;
        velocityY = vy;
    }
}