package org.core.entity;

import lombok.Getter;
import lombok.Setter;
import org.core.collision.Blocker;
import org.core.definition.EnemyProfile;
import org.core.enums.AIState;
import org.core.math.Rect;
import org.core.math.Vec2;
import org.core.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Enemy extends Entity implements Damageable, Blocker {
    private int hp;
    private boolean alive = true;
    private boolean bodyInvestigated = false;

    private Weapon currentWeapon;
    private final EnemyProfile profile;

    private final String enemyId;

    private AIState currentState;
    private float facingAngle;
    @Setter
    private float reactionTimer;
    @Setter
    private float aimMemoryTimer;
    @Setter
    private float shotCommitTimer;
    @Setter
    private float knockbackVelocityX, knockbackVelocityY;
    @Setter
    private float lastKnownPlayerX, lastKnownPlayerY;
    private List<Vec2> currentPath;

    public Enemy(float x, float y, float width, float height,
                 EnemyProfile profile, Weapon weapon, String enemyId) {
        super(x, y, width, height);
        this.profile = profile;
        this.hp = profile.getHp();
        this.currentWeapon = weapon;
        this.enemyId = enemyId;
        this.alive = true;
        this.bodyInvestigated = false;
        this.currentState = AIState.PATROL;
        this.facingAngle = 0f;
        this.currentPath = new ArrayList<>();
    }
    public void applyDamage(int damage){
        hp = Math.max(0, hp - damage);
        if (hp == 0) {
            alive = false;
        }
    }
    public void changeState(AIState state) {
        currentState = state;
    }
    public void setReactionTimer(float reactionTimer) {
        this.reactionTimer = Math.max(0f, reactionTimer);
    }

    @Override
    public Rect getBounds() {
        return Rect.fromCenter(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean blocksMovement() {
        return false;
    }

    @Override
    public boolean blocksVision() {
        return false;
    }

    @Override
    public boolean blocksProjectile() {
        return true;
    }

    @Override
    public boolean blocksSound() {
        return false;
    }

    @Override
    public float getSoundAttenuationFactor() {
        return 1;
    }
    public float getCurrentFovAngle() {
        return currentState == AIState.PATROL ? profile.getPatrolFovAngle() : profile.getAlertFovAngle();
    }
    public void updateReactionTimer(float delta){
        reactionTimer -= delta;
    }
    public void updateAimMemoryTimer(float delta){
        aimMemoryTimer -= delta;
    }
    public void updateShotCommitTimer(float delta){
        shotCommitTimer -= delta;
    }
    public boolean isReactionTimer(){
        return reactionTimer <= 0;
    }
    public boolean isAimMemoryTimer(){
        return aimMemoryTimer <= 0;
    }
    public boolean isShotCommitTimer(){
        return shotCommitTimer <= 0;
    }
    public void resetReactionTimer(){
        reactionTimer = profile.getReactionTime();
    }
    public void resetAimMemoryTimer(){
        aimMemoryTimer = profile.getAimMemoryDuration();
    }
    public void resetShotCommitTimer(){
        shotCommitTimer = profile.getShotCommitDuration();
    }
    public void setLastKnownPlayerPosition(float x, float y){
        lastKnownPlayerX = x;
        lastKnownPlayerY = y;
    }

}