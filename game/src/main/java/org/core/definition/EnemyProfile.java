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

    public EnemyProfile(String enemyTypeId, int hp, float patrolSpeed,
                        float chaseSpeed, float visionRange, float reactionTime,
                        float shotCommitDuration, float aimMemoryDuration,
                        float preferredAttackRange, AimBehaviorType aimBehaviorType) {
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
    }
}
