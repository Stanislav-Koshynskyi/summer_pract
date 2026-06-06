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
import java.util.Optional;

@Getter
public class Enemy extends Entity implements Damageable, Blocker {
    private int hp;
    private boolean alive = true;
    private boolean bodyInvestigated = false;

    private Weapon currentWeapon;
    private final EnemyProfile profile;

    private final String enemyId;

    @Getter
    @Setter
    private boolean wasDamagedThisFrame = false;

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
    @Getter
    @Setter
    private List<Vec2> currentPath;
    private final List<Vec2> patrolPath;

    @Getter
    @Setter
    private float intendedDx, intendedDy;
    @Getter
    @Setter
    private float searchAngle;
    @Getter
    @Setter
    private int currentPatrolIndex = 0;

    public Enemy(float x, float y, EnemyProfile profile, Weapon weapon, String enemyId, List<Vec2> patrolPath) {
        super(x, y, profile.getWidth(), profile.getHeight());
        this.profile = profile;
        this.hp = profile.getHp();
        this.currentWeapon = weapon;
        this.enemyId = enemyId;
        this.alive = true;
        this.bodyInvestigated = false;
        this.currentState = AIState.PATROL;
        this.facingAngle = 0f;
        this.patrolPath = patrolPath;
        this.currentPath = new ArrayList<>(patrolPath);
    }

    public void applyDamage(int damage) {
        hp = Math.max(0, hp - damage);
        if (hp == 0) {
            alive = false;
        }
        wasDamagedThisFrame = true;
    }
    public void resetDamageFlag(){
        wasDamagedThisFrame = false;
    }
    public boolean isDamaged(){
        return wasDamagedThisFrame;
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
    @Setter
    private boolean shotCommitStarted = false;

    public float getCurrentFovAngle() {
        switch (currentState){
            case ATTACK -> {
                return profile.getAttackFovAngle();
            }
            case PATROL -> {
                return profile.getPatrolFovAngle();
            }
            case null, default ->
            {
                return profile.getAlertFovAngle();
            }
        }
    }

    public void updateReactionTimer(float delta) {
        reactionTimer -= delta;
    }

    public void updateAimMemoryTimer(float delta) {
        aimMemoryTimer -= delta;
    }

    public void updateShotCommitTimer(float delta) {
        shotCommitTimer -= delta;
    }

    public boolean isReactionTimer() {
        return reactionTimer <= 0;
    }

    public boolean isAimMemoryTimer() {
        return aimMemoryTimer <= 0;
    }

    public boolean isShotCommitTimer() {
        return shotCommitTimer <= 0;
    }

    public void resetReactionTimer() {
        reactionTimer = profile.getReactionTime();
    }

    public void resetAimMemoryTimer() {
        aimMemoryTimer = profile.getAimMemoryDuration();
    }

    public void resetShotCommitTimer() {
        shotCommitTimer = profile.getShotCommitDuration();
    }

    public void setLastKnownPlayerPosition(float x, float y) {
        lastKnownPlayerX = x;
        lastKnownPlayerY = y;
    }

    public void rotateTowards(float targetAngleDeg, float delta) {
        float diff = Vec2.angleDiff(this.facingAngle, targetAngleDeg);
        float maxTurn = profile.getTurnRate() * delta;
        if (Math.abs(diff) <= maxTurn) {
            this.facingAngle = targetAngleDeg;
        } else {
            this.facingAngle += Math.signum(diff) * maxTurn;
        }
        facingAngle = facingAngle % 360;
        if (facingAngle > 180)
            facingAngle -= 360;
        else if (facingAngle < -180)
            facingAngle += 360;
    }
    public void setIntendMove(float dx, float dy){
        intendedDx = dx;
        intendedDy = dy;
    }
    public Vec2 getCurrentPatrolTarget(){
        if (patrolPath.isEmpty()) return null;
        return patrolPath.get(currentPatrolIndex % patrolPath.size());
    }
    public void goToNextPatrolPoint(){
        currentPatrolIndex++;
    }
    public boolean isShotCommitReady() {
        return shotCommitTimer <= 0;
    }
    public boolean isShotCommitStarted(){
        return shotCommitStarted;
    }
    public float getCollisionWidth(){
        return getWidth() * 0.99f;
    }
    public float getCollisionHeight(){
        return getHeight() * 0.99f;
    }
    public float getCollisionLeft(){
        return getX() - getCollisionWidth() / 2f;
    }
    public float getCollisionRight(){
        return getX() + getCollisionWidth() / 2f;
    }
    public float getCollisionTop(){
        return getY() + getCollisionHeight() / 2f;
    }
    public float getCollisionBottom(){
        return getY() - getCollisionHeight() / 2f;
    }
}