package org.core.ai;

import org.core.definition.EnemyProfile;
import org.core.entity.Enemy;
import org.core.entity.Entity;
import org.core.entity.Player;
import org.core.enums.MovementMode;
import org.core.enums.RayCastType;
import org.core.enums.TileType;
import org.core.geometry.WorldGeometry;
import org.core.math.Vec2;
import org.core.raycast.RayCastResult;
import org.core.raycast.RayCastSystem;

import java.util.List;
import java.util.Set;

public class VisionSystem {
    private final RayCastSystem rayCastSystem;
    private final WorldGeometry worldGeometry;
    public VisionSystem(RayCastSystem rayCastSystem, WorldGeometry worldGeometry){
        this.rayCastSystem = rayCastSystem;
        this.worldGeometry = worldGeometry;
    }
    public boolean canEnemySee(Enemy from, Entity target, List<Vec2> pointToCheck,
                                     Set<Entity> ignoredEntities) {
        float dist = new Vec2(from.getX(), from.getY()).distanceTo(new Vec2(target.getX(), target.getY()));
        float targetDiagonal = (float) Math.sqrt(Math.pow(target.getWidth(), 2) + Math.pow(target.getHeight(), 2));
        if (dist > from.getProfile().getVisionRange() + targetDiagonal ) return false;
        Vec2 toTarget = new Vec2(target.getX() - from.getX(), target.getY() - from.getY());
        float degreeToTarget = toTarget.angleDeg();
        float enemyFacing = from.getFacingAngle();
        float fov = from.getCurrentFovAngle();
        if (Math.abs(Vec2.angleDiff(enemyFacing, degreeToTarget)) >= fov / 2)
            return false;
        for (Vec2 check: pointToCheck){
            Vec2 normalize = check.copy().
                    sub(new Vec2(from.getX(), from.getY())).normalize();
            RayCastResult result = rayCastSystem.cast(new Vec2(from.getX(), from.getY()), normalize, dist,
                    from, ignoredEntities, RayCastType.VISION);
            if (result.hitBlocker() && result.getTarget() == target) return true;
        }
        return false;
    }
    public boolean canEnemySeePlayer(Enemy from, Player to, Set<Entity> ignoredEntity){
        // якщо гравець крадеться і в укритті і близько то автоматично false
        if (to.getMovementMode() == MovementMode.SNEAK && from.getCurrentState() != AIState.ATTACK){
            float dist = new Vec2(from.getX(), from.getY()).distanceTo(new Vec2(to.getX(), to.getY()));
            if (dist > from.getProfile().getCloseRevealRadius()){
                TileType tile = worldGeometry.getTileAt(to.getX(), to.getY());
                if (tile == TileType.LOW_OBSTACLE) return false;
            }
        }


        List<Vec2> checkPoints= List.of(
                new Vec2(to.getLeft(), to.getBottom()),
                new Vec2(to.getRight(), to.getBottom()),
                new Vec2(to.getLeft(), to.getTop()),
                new Vec2(to.getRight(), to.getTop()),
                new Vec2(to.getX(), to.getY())
        );
        return canEnemySee(from, to, checkPoints, ignoredEntity);
    }
    public boolean canEnemySeeCorps(Enemy from, Enemy corps, Set<Entity> ignoredEntity){
        if (corps.isAlive()) return false;
        List<Vec2> checkPoints = List.of(new Vec2(corps.getX(), corps.getY()));
        return canEnemySee(from, corps, checkPoints, ignoredEntity);
    }
}
