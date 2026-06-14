package org.core.event;

import lombok.Getter;
import org.core.entity.Entity;
import org.core.math.Vec2;
@Getter
public class SoundEvent {
    private final float x, y;
    private final float radius;
    private final Entity who;


    public SoundEvent(float x, float y, float radius, Entity who) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.who = who;
    }
    public Vec2 getPoint(){
        return new Vec2(x, y);
    }
}

