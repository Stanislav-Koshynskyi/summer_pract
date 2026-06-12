package org.client;

public class BulletEffect {
    public float x, y;
    public float velocityX, velocityY;
    public float angle;
    public float lifetime;

    public BulletEffect(float startX, float startY, float angle, float speed, float lifetime) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;
        this.lifetime = lifetime;

        float radians = (float) Math.toRadians(angle);
        this.velocityX = (float) Math.cos(radians) * speed;
        this.velocityY = (float) Math.sin(radians) * speed;
    }

    public void update(float dt) {
        x += velocityX * dt;
        y += velocityY * dt;
        lifetime -= dt;
    }
}
