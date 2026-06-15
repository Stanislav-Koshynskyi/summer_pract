package org.content.registry;

import org.core.definition.EnemyProfile;
import org.core.enums.AimBehaviorType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing and storing enemy profiles.
 * Follows a Static Class pattern with instance-level delegate options for flexibility.
 */
public class EnemyProfileRegistry {
    private static final Map<String, EnemyProfile> profiles = new HashMap<>();

    /**
     * Initializes and registers all enemy profiles based on available sprite assets and roles.
     */
    public static void init() {
        profiles.clear();

        // 1. Regular Enemies
        // Colombian: Standard patrol/chase unit
        registerStatic(new EnemyProfile(
                "Colombian",                     // enemyTypeId
                40,                              // hp
                80.0f,                           // patrolSpeed
                150.0f,                          // chaseSpeed
                300.0f,                          // visionRange
                0.4f,                            // reactionTime
                0.2f,                            // shotCommitDuration
                1.0f,                            // aimMemoryDuration
                5.0f,                            // searchMemoryDuration
                150.0f,                          // preferredAttackRange
                AimBehaviorType.STANDARD,        // aimBehaviorType
                90.0f,                           // patrolFovAngle
                120.0f,                          // alertFovAngle
                360.0f,                          // attackFovAngle
                16.0f,                           // width
                16.0f,                           // height
                20.0f,                           // closeRevealRadius
                180.0f                           // turnRate
        ));

        // EGang: Slightly tougher, faster regular enemy
        registerStatic(new EnemyProfile(
                "EGang",
                80,
                90.0f,
                160.0f,
                320.0f,
                0.35f,
                0.15f,
                1.5f,
                6.0f,
                200.0f,
                AimBehaviorType.STANDARD,
                100.0f,
                130.0f,
                360.0f,
                16.0f,
                16.0f,
                20.0f,
                200.0f
        ));

        // 2. Bosses / Unique Enemies
        // GangLeader: Very fast, fast reaction time, high danger
        registerStatic(new EnemyProfile(
                "GangLeader",
                250,
                100.0f,
                200.0f,
                350.0f,
                0.25f,
                0.1f,
                2.0f,
                8.0f,
                220.0f,
                AimBehaviorType.STANDARD,
                110.0f,
                140.0f,
                360.0f,
                16.0f,
                16.0f,
                25.0f,
                240.0f
        ));

        // SwatBoss: Highly armored (high HP), slightly slower, extreme vision and reflexes
        registerStatic(new EnemyProfile(
                "SwatBoss",
                300,
                70.0f,
                130.0f,
                400.0f,
                0.2f,
                0.1f,
                2.5f,
                10.0f,
                250.0f,
                AimBehaviorType.STANDARD,
                120.0f,
                150.0f,
                360.0f,
                18.0f,
                18.0f,
                30.0f,
                160.0f
        ));

        // BearBoss: Giant tank, slower but lethal at close range
        registerStatic(new EnemyProfile(
                "BearBoss",
                500,
                60.0f,
                120.0f,
                250.0f,
                0.5f,
                0.3f,
                1.5f,
                7.0f,
                100.0f,
                AimBehaviorType.STANDARD,
                80.0f,
                100.0f,
                360.0f,
                24.0f,
                24.0f,
                40.0f,
                120.0f
        ));
        // Alias numeric IDs for TMX Map compatibility
        registerStatic("1", getStatic("Colombian"));
        registerStatic("2", getStatic("EGang"));
        registerStatic("3", getStatic("GangLeader"));
    }

    /**
     * Registers an enemy profile in the static collection.
     * @param profile the enemy profile to register
     */
    public static void registerStatic(EnemyProfile profile) {
        profiles.put(profile.getEnemyTypeId(), profile);
    }

    /**
     * Registers an enemy profile in the static collection under a custom alias ID.
     * @param alias the alias ID
     * @param profile the enemy profile to register
     */
    public static void registerStatic(String alias, EnemyProfile profile) {
        profiles.put(alias, profile);
    }

    /**
     * Instance wrapper to register an enemy profile.
     * @param profile the enemy profile to register
     */
    public void register(EnemyProfile profile) {
        registerStatic(profile);
    }

    /**
     * Gets an enemy profile by its unique type ID from the static collection.
     * @param id the unique enemy type ID
     * @return the enemy profile
     * @throws IllegalArgumentException if the enemy profile ID is unknown
     */
    public static EnemyProfile getStatic(String id) {
        EnemyProfile profile = profiles.get(id);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown enemy type id: " + id);
        }
        return profile;
    }

    /**
     * Instance wrapper to get an enemy profile.
     * @param id the unique enemy type ID
     * @return the enemy profile
     */
    public EnemyProfile get(String id) {
        return getStatic(id);
    }

    /**
     * Returns a read-only view of all registered enemy profiles.
     * @return map of all enemy profiles
     */
    public static Map<String, EnemyProfile> getAllStatic() {
        return Collections.unmodifiableMap(profiles);
    }

    /**
     * Instance wrapper to get all enemy profiles.
     * @return map of all enemy profiles
     */
    public Map<String, EnemyProfile> getAll() {
        return getAllStatic();
    }
}
