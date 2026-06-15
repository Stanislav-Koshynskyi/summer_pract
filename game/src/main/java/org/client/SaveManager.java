package org.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {
    private static final String PREFS_NAME = "onebyone_progress";
    private static final String KEY_MAX_LEVEL = "maxUnlockedLevel";

    private final Preferences prefs;

    public SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public int getMaxUnlockedLevel() {
        return prefs.getInteger(KEY_MAX_LEVEL, 1); // якщо немає то 1
    }

    public void setMaxUnlockedLevel(int level) {
        prefs.putInteger(KEY_MAX_LEVEL, level);
        prefs.flush();
    }
}
