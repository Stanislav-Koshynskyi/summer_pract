package org.client.menu.settings;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class StandardPage implements SettingsPage {
    private final String nameUa, nameEn;
    private final List<SettingsItem> items = new ArrayList<>();

    public StandardPage(String nameUa, String nameEn) {
        this.nameUa = nameUa;
        this.nameEn = nameEn;
    }

    public void addItem(SettingsItem item) { items.add(item); }

    @Override public String getName(boolean isUa) { return isUa ? nameUa : nameEn; }

    @Override
    public void handleInput(float mx, float my, boolean jt, boolean it) {
        float currentY = 720 - 120; // WORLD_H (720) - 120
        for (SettingsItem item : items) {
            item.handleInput(mx, my, jt, it, 260 + 50, currentY); // sidebarWidth (260) + 50
            currentY -= item.getHeight();
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float sidebarW, float topBarH, boolean isUa) {
        float currentY = 720 - 120;
        for (SettingsItem item : items) {
            item.render(batch, shapes, fontL, fontM, fontS, sidebarW + 50, currentY, isUa);
            currentY -= item.getHeight();
        }
    }
}
