package org.core.data;

public class EnemySpawnData {
    public float x, y;
    public String enemyTypeId;
    public String weaponId;
    public String patrolPathId;
    public float facingAngle;
    public String enemyId;

    public EnemySpawnData(float x, float y, String enemyTypeId, String weaponId,
                          String patrolPathId, float facingAngle, String enemyId) {
        this.x = x;
        this.y = y;
        this.enemyTypeId = enemyTypeId;
        this.weaponId = weaponId;
        this.patrolPathId = patrolPathId;
        this.facingAngle = facingAngle;
        this.enemyId = enemyId;
    }
}


