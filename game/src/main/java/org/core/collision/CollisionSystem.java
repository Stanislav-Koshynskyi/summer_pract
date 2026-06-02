package org.core.collision;

import org.core.entity.Entity;
import org.core.geometry.WorldGeometry;
import org.core.math.Rect;

import java.util.List;

public class CollisionSystem {

    private final WorldGeometry geometry;
    private final List<Blocker> blockers;

    // Мікро-відступ для запобігання "залипанню" на швах
    private static final float EPSILON = 0.001f;

    public CollisionSystem(WorldGeometry geometry, List<Blocker> blockers) {
        this.geometry = geometry;
        this.blockers = blockers;
    }
    public void move(Entity entity, float dx, float dy) {
        if (dx != 0f) moveX(entity, dx);
        if (dy != 0f) moveY(entity, dy);
    }

    public void applyKnockback(Entity entity, float delta, float friction) {
        float vx = entity.getVelocityX();
        float vy = entity.getVelocityY();
        if (Math.abs(vx) < 0.01f && Math.abs(vy) < 0.01f) return;

        move(entity, vx * delta, vy * delta);

        float decay = Math.max(0f, 1f - friction * delta);
        entity.setVelocity(vx * decay, vy * decay);

        if (Math.abs(entity.getVelocityX()) < 0.5f && Math.abs(entity.getVelocityY()) < 0.5f) {
            entity.setVelocity(0f, 0f);
        }
    }

    public boolean canStandAt(float cx, float cy, float width, float height) {
        Rect aabb = Rect.fromCenter(cx, cy, width, height);
        return !collidesWithTiles(aabb) && !collidesWithBlockers(aabb);
    }

    private void moveX(Entity entity, float dx) {
        float newX = entity.getX() + dx;
        Rect aabb = Rect.fromCenter(newX, entity.getY(), entity.getWidth(), entity.getHeight());
        float maxPen = 0f;

        float ts = geometry.getTileSize();
        int minTX = (int) Math.floor(aabb.getLeft() / ts);
        int maxTX = (int) Math.floor(aabb.getRight() / ts);
        int minTY = (int) Math.floor(aabb.getBottom() / ts);
        int maxTY = (int) Math.floor(aabb.getTop() / ts);

        for (int ty = minTY; ty <= maxTY; ty++) {
            for (int tx = minTX; tx <= maxTX; tx++) {
                if (geometry.getTile(tx, ty).canPass()) continue;
                float left = tx * ts, right = (tx + 1) * ts;
                if (aabb.getRight() > left && aabb.getLeft() < right &&
                        aabb.getTop() > ty*ts && aabb.getBottom() < (ty+1)*ts) {

                    if (dx > 0 && left > entity.getRight() - EPSILON) {
                        float pen = aabb.getRight() - left;
                        if (pen > maxPen) maxPen = pen;
                    } else if (dx < 0 && right < entity.getLeft() + EPSILON) {
                        float pen = right - aabb.getLeft();
                        if (pen > maxPen) maxPen = pen;
                    }
                }
            }
        }
        for (Blocker b : blockers) {
            if (b == entity) continue; // потім вороги теж будуть блокерами і тоді блокер був би сам в собі і не міг би рухатись
            if (!b.blocksMovement()) continue;
            Rect br = b.getBounds();
            if (aabb.overlaps(br)) {
                if (dx > 0 && br.getLeft() > entity.getRight() - EPSILON) {
                    float pen = aabb.getRight() - br.getLeft();
                    if (pen > maxPen) maxPen = pen;
                } else if (dx < 0 && br.getRight() < entity.getLeft() + EPSILON) {
                    float pen = br.getRight() - aabb.getLeft();
                    if (pen > maxPen) maxPen = pen;
                }
            }
        }

        if (maxPen > 0f) {
            if (dx > 0) entity.setX(newX - maxPen - EPSILON);
            else        entity.setX(newX + maxPen + EPSILON);
        } else {
            entity.setX(newX);
        }
    }

    private void moveY(Entity entity, float dy) {
        float newY = entity.getY() + dy;
        Rect aabb = Rect.fromCenter(entity.getX(), newY, entity.getWidth(), entity.getHeight());
        float maxPen = 0f;

        float ts = geometry.getTileSize();
        int minTX = (int) Math.floor(aabb.getLeft() / ts);
        int maxTX = (int) Math.floor(aabb.getRight() / ts);
        int minTY = (int) Math.floor(aabb.getBottom() / ts);
        int maxTY = (int) Math.floor(aabb.getTop() / ts);

        for (int ty = minTY; ty <= maxTY; ty++) {
            for (int tx = minTX; tx <= maxTX; tx++) {
                if (geometry.getTile(tx, ty).canPass()) continue;
                float tileBottom = ty * ts;
                float tileTop = (ty + 1) * ts;

                if (aabb.getRight() > tx * ts && aabb.getLeft() < (tx + 1) * ts &&
                        aabb.getTop() > tileBottom && aabb.getBottom() < tileTop) {

                    if (dy > 0 && tileBottom > entity.getTop() - EPSILON) {
                        float pen = aabb.getTop() - tileBottom;
                        if (pen > maxPen) maxPen = pen;
                    } else if (dy < 0 && tileTop < entity.getBottom() + EPSILON) {
                        float pen = tileTop - aabb.getBottom();
                        if (pen > maxPen) maxPen = pen;
                    }
                }
            }
        }

        for (Blocker b : blockers) {
            if (b == entity) continue;
            if (!b.blocksMovement()) continue;
            Rect br = b.getBounds();
            if (aabb.overlaps(br)) {
                if (dy > 0 && br.getBottom() > entity.getTop() - EPSILON) {
                    float pen = aabb.getTop() - br.getBottom();
                    if (pen > maxPen) maxPen = pen;
                } else if (dy < 0 && br.getTop() < entity.getBottom() + EPSILON) {
                    float pen = br.getTop() - aabb.getBottom();
                    if (pen > maxPen) maxPen = pen;
                }
            }
        }

        if (maxPen > 0f) {
            if (dy > 0) entity.setY(newY - maxPen - EPSILON);
            else        entity.setY(newY + maxPen + EPSILON);
        } else {
            entity.setY(newY);
        }
    }
    private boolean collidesWithTiles(Rect aabb) {
        float ts = geometry.getTileSize();
        int minTX = (int) Math.floor(aabb.getLeft() / ts);
        int maxTX = (int) Math.floor(aabb.getRight() / ts);
        int minTY = (int) Math.floor(aabb.getBottom() / ts);
        int maxTY = (int) Math.floor(aabb.getTop() / ts);

        for (int ty = minTY; ty <= maxTY; ty++) {
            for (int tx = minTX; tx <= maxTX; tx++) {
                if (!geometry.getTile(tx, ty).canPass()) {
                    float left = tx * ts;
                    float right = (tx + 1) * ts;
                    float bottom = ty * ts;
                    float top = (ty + 1) * ts;

                    if (aabb.getRight() > left && aabb.getLeft() < right &&
                            aabb.getTop() > bottom && aabb.getBottom() < top) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean collidesWithBlockers(Rect aabb) {
        for (Blocker blocker : blockers) {
            if (!blocker.blocksMovement()) continue;
            if (aabb.overlaps(blocker.getBounds())) return true;
        }
        return false;
    }
}


