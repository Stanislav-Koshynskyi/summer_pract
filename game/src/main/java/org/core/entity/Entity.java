package org.core.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Entity {
    @Setter
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
    public float getLeft(){
        return x - width / 2f;
    }
    public float getRight(){
        return x + width / 2f;
    }
    public float getTop(){
        return y + height / 2f;
    }
    public float getBottom(){
        return y - height / 2f;
    }
    public float getCollisionWidth(){
        return width;
    }
    public float getCollisionHeight(){
        return height;
    }
    public float getCollisionLeft(){
        return x - getCollisionWidth() / 2f;
    }
    public float getCollisionRight(){
        return x + getCollisionWidth() / 2f;
    }
    public float getCollisionTop(){
        return y + getCollisionHeight() / 2f;
    }
    public float getCollisionBottom(){
        return y - getCollisionHeight() / 2f;
    }

}