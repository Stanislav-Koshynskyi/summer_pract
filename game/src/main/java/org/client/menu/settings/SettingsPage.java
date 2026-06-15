package org.client.menu.settings;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface SettingsPage {
    String getName(boolean isUa);
    void handleInput(float mx, float my, boolean justTouched, boolean isTouched);
    void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float sidebarW, float topBarH, boolean isUa);
}
