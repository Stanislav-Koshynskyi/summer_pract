package org.client;

import com.badlogic.gdx.graphics.Color;

public class VisualAttackEffect {
    float fromX, fromY, toX, toY;
    float lifetime;
    Color color;

    public VisualAttackEffect(float fromX, float fromY, float toX, float toY, float lifetime, Color color) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.lifetime = lifetime;
        this.color = color;
    }
}
