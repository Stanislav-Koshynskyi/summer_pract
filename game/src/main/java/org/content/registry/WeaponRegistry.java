package org.content.registry;

import org.content.weapon_behavior.MeleeBehavior;
import org.content.weapon_behavior.ShotgunBehavior;
import org.core.definition.WeaponDefinition;
import org.core.enums.WeaponType;
import org.content.weapon_behavior.SimpleRayCastBehavior;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing and storing weapon definitions.
 * Follows a Static Class pattern with instance-level delegate options for flexibility.
 */
public class WeaponRegistry {
    private static final Map<String, WeaponDefinition> weapons = new HashMap<>();

    /**
     * Initializes and registers all weapons in the game.
     */
    public static void init() {
        weapons.clear();

        // 1. Melee Weapons
        // Knife: Quiet, fast, short range, high damage (instant kill if close)
        registerStatic(new WeaponDefinition(
                "Knife",
                WeaponType.MELEE,
                100,             // damage
                40.0f,           // range
                0.3f,            // cooldown (seconds)
                -1,              // maxAmmo (-1 for infinite)
                true,            // isSuppressed
                false,           // isAutomatic
                new MeleeBehavior(),
                5.0f,            // knockbackForce
                0.0f             // spread
        ));

        // Bat: Long range melee, slower swing, high knockback
        registerStatic(new WeaponDefinition(
                "Bat",
                WeaponType.MELEE,
                60,
                55.0f,
                0.6f,
                -1,
                false,
                false,
                new MeleeBehavior(),
                15.0f,
                0.0f
        ));

        // Pipe: Average melee weapon
        registerStatic(new WeaponDefinition(
                "Pipe",
                WeaponType.MELEE,
                50,
                50.0f,
                0.7f,
                -1,
                false,
                false,
                new MeleeBehavior(),
                12.0f,
                0.0f
        ));

        // Machete: Deadly blade weapon, fast and high damage
        registerStatic(new WeaponDefinition(
                "Machete",
                WeaponType.MELEE,
                110,
                45.0f,
                0.45f,
                -1,
                true,
                false,
                new MeleeBehavior(),
                8.0f,
                0.0f
        ));

        // 2. Firearms
        // 9mm: Standard pistol, balanced stats
        registerStatic(new WeaponDefinition(
                "9mm",
                WeaponType.HITSCAN,
                60,
                500.0f,
                0.40f,
                8,
                false,
                false,
                new SimpleRayCastBehavior(),
                10.0f,
                0.05f
        ));

        // Silencer: Quiet pistol, reduced damage and range
        registerStatic(new WeaponDefinition(
                "Silencer",
                WeaponType.HITSCAN,
                30,
                450.0f,
                0.3f,
                15,
                true,
                false,
                new SimpleRayCastBehavior(),
                5.0f,
                0.04f
        ));

        // Shotgun: Huge spread, massive damage, short range, slow reload/cooldown
        registerStatic(new WeaponDefinition(
                "Shotgun",
                WeaponType.HITSCAN,
                30,
                250.0f,
                0.8f,
                3,
                false,
                false,
                new ShotgunBehavior(),
                30.0f,
                0.3f
        ));

        // Uzi: Rapid fire, high spread, low damage
        registerStatic(new WeaponDefinition(
                "Uzi",
                WeaponType.HITSCAN,
                25,
                350.0f,
                0.08f,
                10,
                false,
                true,
                new SimpleRayCastBehavior(),
                4.0f,
                0.15f
        ));

        // Famae: Automatic assault rifle, high damage, long range
        registerStatic(new WeaponDefinition(
                "Famae",
                WeaponType.HITSCAN,
                30,
                600.0f,
                0.16f,
                25,
                false,
                true,
                new SimpleRayCastBehavior(),
                15.0f,
                0.08f
        ));
        // Alias numeric IDs for TMX Map compatibility
        registerStatic("1", getStatic("Knife"));
        registerStatic("2", getStatic("9mm"));
        registerStatic("3", getStatic("Shotgun"));
        registerStatic("4", getStatic("Uzi"));
        registerStatic("5", getStatic("Famae"));
    }

    /**
     * Registers a weapon in the static collection.
     * @param weapon the weapon definition to register
     */
    public static void registerStatic(WeaponDefinition weapon) {
        weapons.put(weapon.getId(), weapon);
    }

    /**
     * Registers a weapon in the static collection under a custom alias ID.
     * @param alias the alias ID
     * @param weapon the weapon definition to register
     */
    public static void registerStatic(String alias, WeaponDefinition weapon) {
        weapons.put(alias, weapon);
    }

    /**
     * Instance wrapper to register a weapon definition.
     * @param weapon the weapon definition to register
     */
    public void register(WeaponDefinition weapon) {
        registerStatic(weapon);
    }

    /**
     * Gets a weapon definition by its unique identifier from the static collection.
     * @param id the unique weapon ID
     * @return the weapon definition
     * @throws IllegalArgumentException if the weapon ID is unknown
     */
    public static WeaponDefinition getStatic(String id) {
        WeaponDefinition weapon = weapons.get(id);
        if (weapon == null) {
            throw new IllegalArgumentException("Unknown weapon type id: " + id);
        }
        return weapon;
    }

    /**
     * Instance wrapper to get a weapon definition.
     * @param id the unique weapon ID
     * @return the weapon definition
     */
    public WeaponDefinition get(String id) {
        return getStatic(id);
    }

    /**
     * Returns a read-only view of all registered weapons.
     * @return map of all weapon definitions
     */
    public static Map<String, WeaponDefinition> getAllStatic() {
        return Collections.unmodifiableMap(weapons);
    }

    /**
     * Instance wrapper to get all weapon definitions.
     * @return map of all weapon definitions
     */
    public Map<String, WeaponDefinition> getAll() {
        return getAllStatic();
    }
}
