package org.core.definition;


import lombok.Getter;
import org.core.enums.AimBehaviorType;

@Getter
public class EnemyProfile {
    private final String enemyTypeId;
    private final int hp;
    private final float patrolSpeed;
    private final float chaseSpeed;
    private final float visionRange;
    private final float reactionTime;
    private final float shotCommitDuration;
    private final float aimMemoryDuration;
    private final float preferredAttackRange;
    private final AimBehaviorType aimBehaviorType;
    private final float patrolFovAngle;
    private final float alertFovAngle;
    private final float width;
    private final float height;

    public EnemyProfile(String enemyTypeId, int hp, float patrolSpeed,
                        float chaseSpeed, float visionRange, float reactionTime,
                        float shotCommitDuration, float aimMemoryDuration,
                        float preferredAttackRange, AimBehaviorType aimBehaviorType, float patrolFovAngle,
                        float alertFovAngle, float width, float height) {
        this.enemyTypeId = enemyTypeId;
        this.hp = hp;
        this.patrolSpeed = patrolSpeed;
        this.chaseSpeed = chaseSpeed;
        this.visionRange = visionRange;
        this.reactionTime = reactionTime;
        this.shotCommitDuration = shotCommitDuration;
        this.aimMemoryDuration = aimMemoryDuration;
        this.preferredAttackRange = preferredAttackRange;
        this.aimBehaviorType = aimBehaviorType;
        this.patrolFovAngle = patrolFovAngle;
        this.alertFovAngle = alertFovAngle;
        this.width = width;
        this.height = height;
    }
}
