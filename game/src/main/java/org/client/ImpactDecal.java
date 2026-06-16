package org.client;

/**
 * A temporary visual decal spawned when a bullet impacts a wall or enemy.
 * Blood splatters for entity hits, spark marks for wall hits.
 * Lasts 60 seconds then fades out.
 */
public class ImpactDecal {
    public float x, y;
    public float angle;         // direction the bullet was traveling
    public boolean isBlood;     // true = blood splatter, false = wall spark/bullet hole
    public float lifetime;      // seconds remaining
    public float maxLifetime;   // initial lifetime for fade calculation
    public float scale;         // random scale for visual variety

    // Random offsets for multi-splat rendering (set at creation time)
    public float[] splatOffsetsX;
    public float[] splatOffsetsY;
    public float[] splatSizes;

    public ImpactDecal(float x, float y, float angle, boolean isBlood) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.isBlood = isBlood;
        this.lifetime = 60f;
        this.maxLifetime = 60f;
        this.scale = 0.8f + (float) Math.random() * 0.4f;

        // Generate random splat pattern
        int splatCount = isBlood ? 4 + (int)(Math.random() * 3) : 2 + (int)(Math.random() * 2);
        splatOffsetsX = new float[splatCount];
        splatOffsetsY = new float[splatCount];
        splatSizes = new float[splatCount];

        float spread = isBlood ? 8f : 4f;
        for (int i = 0; i < splatCount; i++) {
            splatOffsetsX[i] = ((float) Math.random() - 0.5f) * spread * 2f;
            splatOffsetsY[i] = ((float) Math.random() - 0.5f) * spread * 2f;
            splatSizes[i] = (isBlood ? 2f : 1.5f) + (float) Math.random() * (isBlood ? 3f : 2f);
        }
    }
}
