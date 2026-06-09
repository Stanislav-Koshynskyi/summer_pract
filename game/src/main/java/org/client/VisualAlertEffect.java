package org.client;

import org.core.entity.Enemy;

public class VisualAlertEffect {
    public final Enemy enemy;
    public float lifetime;

    public VisualAlertEffect(Enemy enemy, float lifetime) {
        this.enemy = enemy;
        this.lifetime = lifetime;
    }
}