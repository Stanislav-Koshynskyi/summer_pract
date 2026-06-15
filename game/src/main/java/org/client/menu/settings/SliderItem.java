package org.client.menu.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SliderItem extends SettingsItem {
    private final String titleUa, titleEn, descUa, descEn;
    private float value;
    private boolean isDragging = false;
    private final float sliderW = 350, sliderH = 15;
    private final Color ACCENT_GOLD = new Color(0xffd700ff);

    public SliderItem(String tUa, String tEn, String dUa, String dEn, float defVal) {
        this.titleUa = tUa; this.titleEn = tEn;
        this.descUa = dUa; this.descEn = dEn;
        this.value = defVal;
    }

    @Override public float getHeight() { return 170; }

    @Override
    public void handleInput(float mx, float my, boolean jt, boolean it, float startX, float startY) {
        float sliderY = startY - 140;
        if (jt) {
            if (mx >= startX - 20 && mx <= startX + sliderW + 20 && my >= sliderY - 20 && my <= sliderY + sliderH + 20) {
                isDragging = true;
            }
        }
        if (it && isDragging) {
            value = (mx - startX) / sliderW;
            if (value < 0) value = 0;
            if (value > 1) value = 1;
        } else if (!it) {
            isDragging = false;
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float startX, float startY, boolean isUa) {
        float titleY = startY - 25, descY = startY - 60, labelY = startY - 100, sliderY = startY - 140;

        batch.begin();
        fontL.setColor(Color.WHITE); fontL.draw(batch, isUa ? titleUa : titleEn, startX, titleY);
        fontS.setColor(Color.GRAY); fontS.draw(batch, isUa ? descUa : descEn, startX, descY);
        fontM.setColor(ACCENT_GOLD); fontM.draw(batch, isUa ? "Гучність" : "Volume", startX, labelY);
        fontM.draw(batch, (int)(value * 100) + "%", startX + sliderW + 30, labelY);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0x3a3f52ff)); shapes.rect(startX, sliderY, sliderW, sliderH);
        shapes.setColor(ACCENT_GOLD);
        float filledWidth = sliderW * value;
        if (filledWidth > 0) shapes.rect(startX, sliderY, filledWidth, sliderH);
        shapes.circle(startX + filledWidth, sliderY + sliderH / 2, 14);
        shapes.end();
    }
}
