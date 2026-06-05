package org.core.ai;

import org.core.collision.Blocker;
import org.core.collision.CollisionSystem;
import org.core.collision.PathBlocker;
import org.core.entity.Enemy;
import org.core.enums.TileType;
import org.core.geometry.WorldGeometry;
import org.core.math.Vec2;

import java.util.*;

public class PathFinder {

    private static final float STRAIGHT_COST = 1f;
    private static final float DIAGONAL_COST = 1.414f;

    private static final int[][] DIRS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private final WorldGeometry geometry;
    private final CollisionSystem collisionSystem;
    private final List<Blocker> blockers;   // тепер загальний список

    public PathFinder(WorldGeometry geometry, CollisionSystem collisionSystem, List<Blocker> blockers) {
        this.geometry = geometry;
        this.collisionSystem = collisionSystem;
        this.blockers = blockers;
    }

    public List<Vec2> findPath(Enemy enemy, float targetWorldX, float targetWorldY, List<Blocker> ignoredBlockers) {
        float ts = geometry.getTileSize();
        float ew = enemy.getWidth();
        float eh = enemy.getHeight();

        int startTX = (int) Math.floor(enemy.getX() / ts);
        int startTY = (int) Math.floor(enemy.getY() / ts);
        int goalTX = (int) Math.floor(targetWorldX / ts);
        int goalTY = (int) Math.floor(targetWorldY / ts);

        if (startTX == goalTX && startTY == goalTY) return Collections.emptyList();

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Long, Node> openMap = new HashMap<>();
        Map<Long, Node> closed = new HashMap<>();

        Node start = new Node(startTX, startTY, 0f, heuristic(startTX, startTY, goalTX, goalTY), null);
        open.add(start);
        openMap.put(key(startTX, startTY), start);

        while (!open.isEmpty()) {
            Node current = open.poll();
            openMap.remove(key(current.tx, current.ty));

            if (current.tx == goalTX && current.ty == goalTY) {
                return reconstructPath(current, ts);
            }

            closed.put(key(current.tx, current.ty), current);

            for (int[] dir : DIRS) {
                int nx = current.tx + dir[0];
                int ny = current.ty + dir[1];
                long nKey = key(nx, ny);

                if (closed.containsKey(nKey)) continue;

                float tileCX = nx * ts + ts * 0.5f;
                float tileCY = ny * ts + ts * 0.5f;

                boolean isGoal = (nx == goalTX && ny == goalTY);
                TileType tileType = geometry.getTile(nx, ny);
                float tileCost = tileType.getPathCost();
                // якщо не прохідний то не рахуємо
                if (tileCost >= Float.MAX_VALUE) continue;
                if (!isGoal) {

                    boolean blockedByBlocker = false;
                    for (Blocker blocker : blockers) {
                        if (!blocker.getBounds().contains(tileCX, tileCY)) continue;
                        if (ignoredBlockers.contains(blocker)) continue;
                        if (blocker instanceof PathBlocker pathBlocker) {
                            if (!pathBlocker.isTraversable()) {
                                blockedByBlocker = true;
                                break;
                            }
                            // тут треба буде прикинути як краще, брати max або сумувати вартості
                            // поки так бо простіше
                            tileCost = Math.max(tileCost, pathBlocker.getPathCost());
                        } else if (blocker.blocksMovement()) {
                            blockedByBlocker = true;
                            break;
                        }
                    }
                    if (blockedByBlocker) continue;

                    // Стандартна колізія (тайли, що не блокують рух, але можуть бути завузькими)
                    if (!isTileWalkable(tileCX, tileCY, ew, eh, ignoredBlockers)) continue;

                    // перевірка кутів, якщо хочемо йти по діагоналі, але проходу немає
                    if (dir[0] != 0 && dir[1] != 0){
                        float cx1 = (current.tx + dir[0]) * ts + ts * 0.5f;
                        float cy1 = current.ty * ts + ts * 0.5f;
                        float cx2 = current.tx * ts + ts * 0.5f;
                        float cy2 = (current.ty + dir[1]) * ts + ts * 0.5f;
                        if (!isTileWalkable(cx1, cy1, ew, eh, ignoredBlockers) ||
                                !isTileWalkable(cx2, cy2, ew, eh, ignoredBlockers)) continue;
                    }
                }
                float moveCost = (dir[0] != 0 && dir[1] != 0) ? DIAGONAL_COST : STRAIGHT_COST;
                float newG = current.g + moveCost * tileCost;
                float newH = heuristic(nx, ny, goalTX, goalTY);
                float newF = newG + newH;

                Node existing = openMap.get(nKey);
                if (existing != null && existing.g <= newG) continue;

                Node neighbor = new Node(nx, ny, newG, newF, current);
                open.add(neighbor);
                openMap.put(nKey, neighbor);

            }
        }

        return Collections.emptyList();
    }

    private float heuristic(int tx, int ty, int gx, int gy) {
        int dx = Math.abs(tx - gx);
        int dy = Math.abs(ty - gy);
        return STRAIGHT_COST * (dx + dy) + (DIAGONAL_COST - 2 * STRAIGHT_COST) * Math.min(dx, dy);
        // min * diagonal + |max - min| * straight, просто оптимізований, для розрахунку
    }

    private long key(int tx, int ty) {
        return ((long) tx << 32) | (ty & 0xFFFFFFFFL);
    }

    private List<Vec2> reconstructPath(Node goal, float tileSize) {
        List<Vec2> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            float px = current.tx * tileSize + tileSize * 0.5f;
            float py = current.ty * tileSize + tileSize * 0.5f;
            path.add(new Vec2(px, py));
            current = current.parent;
        }
        Collections.reverse(path);
        if (!path.isEmpty()) path.remove(0);
        return path;
    }

    private static class Node {
        final int tx, ty;
        final float g;
        final float f;
        final Node parent;

        Node(int tx, int ty, float g, float f, Node parent) {
            this.tx = tx;
            this.ty = ty;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }
    }

    public float getTitleSize() {
        return geometry.getTileSize();
    }

    private boolean isTileWalkable(float centerX, float centerY, float width, float height, List<Blocker> ignoredBlockers) {
        float ts = geometry.getTileSize();
        ;

        float halfW = width / 2f;
        float halfH = height / 2f;

        int minTX = (int) Math.floor((centerX - halfW) / ts);
        int maxTX = (int) Math.floor((centerX + halfW) / ts);
        int minTY = (int) Math.floor((centerY - halfH) / ts);
        int maxTY = (int) Math.floor((centerY + halfH) / ts);

        for (int ty = minTY; ty <= maxTY; ty++) {
            for (int tx = minTX; tx <= maxTX; tx++) {
                TileType tile = geometry.getTile(tx, ty);
                if (!tile.canPass()) return false;

                float tileCX = tx * ts + ts / 2f;
                float tileCY = ty * ts + ts / 2f;
                for (Blocker blocker : blockers) {
                    if (ignoredBlockers.contains(blocker)) continue;
                    if (blocker.getBounds().contains(tileCX, tileCY)) {
                        if (blocker instanceof PathBlocker pathBlocker) {
                            if (!pathBlocker.isTraversable()) return false;
                        } else if (blocker.blocksMovement()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}