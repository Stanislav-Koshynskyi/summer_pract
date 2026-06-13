package org.client;

import lombok.Getter;

public class BulletEffect {
    public float x, y;
    public float targetX, targetY;
    public float angle;
    public float speed;
    public float lifetime;

    private float velX;
    private float velY;
    @Getter
    private boolean arrived = false;

    public BulletEffect(float startX, float startY, float targetX, float targetY, float speed, float lifetime) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.speed = speed;
        this.lifetime = lifetime;

        float dx = targetX - startX;
        float dy = targetY - startY;
        this.angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            this.velX = (dx / distance) * speed;
            this.velY = (dy / distance) * speed;
        }
    }

    public void update(float dt) {
        lifetime -= dt;

        float distBeforeSq = (targetX - x) * (targetX - x) + (targetY - y) * (targetY - y);

        x += velX * dt;
        y += velY * dt;

        float distAfterSq = (targetX - x) * (targetX - x) + (targetY - y) * (targetY - y);

        if (distAfterSq > distBeforeSq || distAfterSq < 16f) {
            arrived = true;

            float dirX = velX / speed;
            float dirY = velY / speed;

            float pullback = 10f;

            x = targetX - dirX * pullback;
            y = targetY - dirY * pullback;
        }
    }
}
