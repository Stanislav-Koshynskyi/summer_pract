package org.content.registry;

import org.core.definition.PlayerProfile;
import org.core.definition.WeaponDefinition;
import org.core.weapon.Weapon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing and storing player profiles.
 */
public class PlayerRegistry {
    public static final String DEFAULT_PLAYER_ID = "Default";
    private static final Map<String, PlayerProfile> profiles = new HashMap<>();

    /**
     * Initializes and registers all player profiles based on available mask assets.
     */
    public static void init() {
        WeaponRegistry.init();
        Map<String, WeaponDefinition> weaponRegistry = WeaponRegistry.getAllStatic();

        // 1. Default character
        // No mask. Starts with Silencer
        // No bonuses
        registerStatic(new PlayerProfile("Default",
                new Weapon(weaponRegistry.get("Silencer")),
                new Weapon(weaponRegistry.get("Knife")),
                0, 1, 1, 1, 0, 1,
                "Норміс", "Default",
                "Збалансований боєць. Старт: Пістолет із глушником. Без бонусів.",
                "Balanced fighter. Start: Silenced Pistol. No bonuses."
        ));

        // 2. Rusher
        // Starts with Uzi
        // More hp, more noise, more damage
        registerStatic(new PlayerProfile("Rusher",
                new Weapon(weaponRegistry.get("Uzi")),
                new Weapon(weaponRegistry.get("Knife")),
                1, 1, 1, 1, 10, 1.3f,
                "Штурмовик", "Rusher",
                "Створений для бою. Старт: Uzi. Підвищене здоров'я, шкода і шум.",
                "Built for combat. Start: Uzi. Increased health, damage and noise."
        ));

        // 3. Stealther
        // No starting weapon
        // Less damage, less noise, long hands
        registerStatic(new PlayerProfile("Stealther",
                new Weapon(weaponRegistry.get("Bat")),
                new Weapon(weaponRegistry.get("Bat")),
                0, 1.1f, 1, 1.5f, -20, 0.6f,
                "Тінь", "Shadow",
                "Майстер ближнього бою. Старт: Бита. Довші руки, менше шуму і шкоди.",
                "Melee master. Start: Bat. Longer reach, less noise and damage."
        ));

        // 4. Risky
        // Starts with Shotgun
        // Much more health, more speed, more ammo, slightly less damage, less noise
        registerStatic(new PlayerProfile("Risky",
                new Weapon(weaponRegistry.get("Shotgun")),
                new Weapon(weaponRegistry.get("Knife")),
                2, 1.4f, 1.4f, 1, -5, 0.8f,
                "Мужик з дробовиком", "Risky",
                "Швидкий і смертоносний. Старт: Дробовик. Більше здоров'я, швидкість і патрони.",
                "Fast and deadly. Start: Shotgun. More health, speed and ammo."
        ));
        // 5. Ghost
        registerStatic(new PlayerProfile("Ghost",
                new Weapon(weaponRegistry.get("Knife")),
                new Weapon(weaponRegistry.get("Knife")),
                0, 1.5f, 1.0f, 1.0f, 0, 0.0f,
                "Привид", "Ghost",
                "Невидимий і нечутний. Старт: Ніж. Не може стріляти. Рухається безшумно.",
                "Unseen and unheard. Start: Knife. Cannot shoot. Moves silently."
        ));


    }

    /**
     * Registers a player profile in the static collection.
     * @param profile the player profile to register
     */
    public static void registerStatic(PlayerProfile profile) {
        profiles.put(profile.getId(), profile);
    }

    /**
     * Instance wrapper to register a player profile.
     * @param profile the player profile to register
     */
    public void register(PlayerProfile profile) {
        registerStatic(profile);
    }

    /**
     * Gets a player profile by its unique type ID from the static collection.
     * @param id the unique player ID
     * @return the player profile
     * @throws IllegalArgumentException if the player profile ID is unknown
     */
    public static PlayerProfile getStatic(String id) {
        PlayerProfile profile = profiles.get(id);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown player type id: " + id);
        }
        return profile;
    }

    /**
     * Instance wrapper to get a player profile.
     * @param id the unique player ID
     * @return the enemy profile
     */
    public PlayerProfile get(String id) {
        return getStatic(id);
    }

    /**
     * Returns a read-only view of all registered player profiles.
     * @return map of all player profiles
     */
    public static Map<String, PlayerProfile> getAllStatic() {
        return Collections.unmodifiableMap(profiles);
    }

    /**
     * Instance wrapper to get all player profiles.
     * @return map of all player profiles
     */
    public Map<String, PlayerProfile> getAll() {
        return getAllStatic();
    }
}
