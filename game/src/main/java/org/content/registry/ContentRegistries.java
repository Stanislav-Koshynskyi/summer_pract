package org.content.registry;

/**
 * Master manager for game content registries.
 * Provides global initialization entry points with safety guards.
 */
public class ContentRegistries {
    private static boolean isInitialized = false;

    /**
     * Initializes all game content registries if they have not been initialized yet.
     * This registers weapons and enemy profiles to be available across the application.
     */
    public static synchronized void initAll() {
        if (isInitialized) {
            return;
        }

        WeaponRegistry.init();
        EnemyProfileRegistry.init();

        isInitialized = true;
    }

    /**
     * Checks if the content registries have been initialized.
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Reset the initialization state. Mainly used for testing or reload purposes.
     */
    public static synchronized void reset() {
        isInitialized = false;
    }
}
