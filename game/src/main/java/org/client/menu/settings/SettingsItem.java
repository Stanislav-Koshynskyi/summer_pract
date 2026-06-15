package org.client.menu.settings;


import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class SettingsItem {
    public abstract float getHeight();
    public abstract void handleInput(float mx, float my, boolean jt, boolean it, float startX, float startY);
    public abstract void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float startX, float startY, boolean isUa);
}
