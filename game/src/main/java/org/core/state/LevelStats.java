package org.core.state;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;


public class LevelStats {

    private final Set<String> alertedIds = new HashSet<>();
    private final Set<String> attackIds = new HashSet<>();
    private final Set<String> searchIds = new HashSet<>();

    @Getter
    private int alertCount;

    @Getter
    private boolean gunshotFired;
    @Getter
    private boolean suppressedShotFired;


    @Getter
    private int killCount;
    @Getter
    private int silentKillCount;

    @Getter
    private float elapsedTime;

    public void recordEnemyAlerted(String enemyId) {
        alertedIds.add(enemyId);
        alertCount++;
    }

   public void recordEnemyEnteredAttack(String enemyId) {
        alertedIds.add(enemyId);
        attackIds.add(enemyId);
        alertCount++;
    }

    public void recordEnemyEnteredSearchInvestigate(String enemyId) {
        alertedIds.add(enemyId);
        searchIds.add(enemyId);
        alertCount++;
    }

    public void recordGunshot(boolean suppressed) {
        if (suppressed) suppressedShotFired = true;
        else gunshotFired = true;
    }

    public void recordKill(boolean silent) {
        killCount++;
        if (silent) silentKillCount++;
    }

    public void addElapsedTime(float delta) {
        elapsedTime += delta;
    }


    public int getUniqueEnemiesAlerted() {
        return alertedIds.size();
    }


    public int getUniqueEnemiesEnteredAttack() {
        return attackIds.size();
    }


    public int getUniqueEnemiesEnteredSearchOrInvestigate() {
        return searchIds.size();
    }


    public void reset() {
        alertedIds.clear();
        attackIds.clear();
        searchIds.clear();
        alertCount = 0;
        gunshotFired = false;
        suppressedShotFired = false;
        killCount = 0;
        silentKillCount = 0;
        elapsedTime = 0f;
    }
    public int calculateScore() {
        int score = 0;

        score += killCount * 100;
        score += silentKillCount * 200;

        int parTime = 300;
        int seconds = (int) elapsedTime;
        if (seconds < parTime) {
            score += (parTime - seconds) * 10;
        }

        if (getUniqueEnemiesAlerted() == 0) {
            score += 500;
        }

        int aggressiveKills = getUniqueEnemiesEnteredAttack();
        if (aggressiveKills >= 3) {
            score += aggressiveKills * 150;
        }

        if (gunshotFired) {
            score -= 50;
            if (!suppressedShotFired) {
                score -= 100;
            }
        }

        return Math.max(0, score);
    }
}
