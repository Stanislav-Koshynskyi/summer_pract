package org.core.entity;

import lombok.Getter;
import org.core.definition.EnemyProfile;
import org.core.enums.AIState;
import org.core.math.Vec2;
import org.core.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Enemy extends Entity implements Damageable{
    private int hp;
    private boolean alive = true;
    private boolean bodyInvestigated = false;

    private Weapon currentWeapon;
    private final EnemyProfile profile;

    private final String enemyId;

    private AIState currentState;
    private float facingAngle;

    private float reactionTimer;
    private float aimMemoryTimer;
    private float shotCommitTimer;

    private float knockbackVelocityX, knockbackVelocityY;

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
}