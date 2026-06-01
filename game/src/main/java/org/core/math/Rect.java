package org.core.math;

public class Rect {

    // x, y — лівий нижній кут (LibGDX конвенція, Y знизу вгору)
    public float x, y, width, height;

    public Rect() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    //Створити Rect з центру
    public static Rect fromCenter(float cx, float cy, float width, float height) {
        return new Rect(cx - width / 2f, cy - height / 2f, width, height);
    }
    public Rect set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }
    public Rect setCenter(float cx, float cy) {
        this.x = cx - width / 2f;
        this.y = cy - height / 2f;
        return this;
    }
    public Rect copy() {
        return new Rect(x, y, width, height);
    }


    //Розширити Rect на amount з усіх боків
    public Rect expand(float amount) {
        return new Rect(
                x - amount,
                y - amount,
                width  + amount * 2,
                height + amount * 2
        );
    }

    public float getLeft()   { return x; }
    public float getRight()  { return x + width; }
    public float getBottom() { return y; }
    public float getTop()    { return y + height; }

    public float getCenterX() { return x + width  / 2f; }
    public float getCenterY() { return y + height / 2f; }

    public Vec2 getCenter() {
        return new Vec2(getCenterX(), getCenterY());
    }
    //Перевірка перетину двох AABB — основа CollisionSystem.
    public boolean overlaps(Rect other) {
        return getRight()  > other.getLeft()   &&
                getLeft()   < other.getRight()  &&
                getTop()    > other.getBottom() &&
                getBottom() < other.getTop();
    }
    //Перевірка чи точка всередині Rect.
    public boolean contains(float px, float py) {
        return px >= getLeft()   &&
                px <= getRight()  &&
                py >= getBottom() &&
                py <= getTop();
    }

    public boolean contains(Vec2 point) {
        return contains(point.x, point.y);
    }


    //Перевірка перетину з відрізком (для raycast).
    //Повертає дистанцію до першого перетину або -1 якщо немає.
    // dir це нормалізований вектор (len = 1)

    public float rayCast(Vec2 from, Vec2 dir, float maxDist) {
        float tMin = 0f;
        float tMax = maxDist;

        // X slab
        if (Math.abs(dir.x) > 0.0001f) {
            float t1 = (getLeft()  - from.x) / dir.x;
            float t2 = (getRight() - from.x) / dir.x;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
        } else if (from.x < getLeft() || from.x > getRight()) {
            return -1f;
        }

        // Y slab
        if (Math.abs(dir.y) > 0.0001f) {
            float t1 = (getBottom() - from.y) / dir.y;
            float t2 = (getTop()    - from.y) / dir.y;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
        } else if (from.y < getBottom() || from.y > getTop()) {
            return -1f;
        }

        if (tMax < tMin) return -1f;
        return tMin >= 0 ? tMin : -1f;
    }


    @Override
    public String toString() {
        return "Rect(x=" + x + ", y=" + y + ", w=" + width + ", h=" + height + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Rect other)) return false;
        return Float.compare(x, other.x) == 0 &&
                Float.compare(y, other.y) == 0 &&
                Float.compare(width, other.width) == 0 &&
                Float.compare(height, other.height) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(x);
        result = 17 * result + Float.hashCode(y);
        result = 17 * result + Float.hashCode(width);
        result = 17 * result + Float.hashCode(height);
        return result;
    }
}
