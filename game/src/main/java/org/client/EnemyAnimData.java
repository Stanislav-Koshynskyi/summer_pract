package org.client;

public class EnemyAnimData {
    float stateTime = 0f;
    float lastX;
    float lastY;
    public float lastAngle;
    public String enemyType;
    boolean isMoving;
    AnimationState currentState = AnimationState.IDLE;
}