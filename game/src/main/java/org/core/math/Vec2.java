package org.core.math;

public class Vec2 {

    public float x, y;

    public Vec2() {
        this.x = 0;
        this.y = 0;
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2 set(Vec2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Vec2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2 add(Vec2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vec2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2 sub(Vec2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vec2 scale(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vec2 normalize() {
        float len = length();
        if (len > 0.0001f) {
            this.x /= len;
            this.y /= len;
        }
        return this;
    }

    public Vec2 copy() {
        return new Vec2(this.x, this.y);
    }


    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public float distanceTo(Vec2 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float distanceTo(float ox, float oy) {
        float dx = this.x - ox;
        float dy = this.y - oy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float dot(Vec2 other) {
        return this.x * other.x + this.y * other.y;
    }

    //Кут вектора в градусах відносно осі X (0° = правo, 90° = вгору).
    public float angleDeg() {
        return (float) Math.toDegrees(Math.atan2(y, x));
    }

    //Кут від цього вектора до target в градусах.

    public float angleToward(Vec2 target) {
        return (float) Math.toDegrees(Math.atan2(target.y - this.y, target.x - this.x));
    }


    //Різниця кутів у діапазоні [-180, 180].
    public static float angleDiff(float fromDeg, float toDeg) {
        float diff = (toDeg - fromDeg) % 360f;
        if (diff > 180f)  diff -= 360f;
        if (diff < -180f) diff += 360f;
        return diff;
    }

    //Одиничний вектор за кутом у градусах.
    public static Vec2 fromAngleDeg(float degrees) {
        float rad = (float) Math.toRadians(degrees);
        return new Vec2((float) Math.cos(rad), (float) Math.sin(rad));
    }


    @Override
    public String toString() {
        return "Vec2(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vec2 other)) return false;
        return Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return 17 * Float.hashCode(x) + Float.hashCode(y);
    }
}
