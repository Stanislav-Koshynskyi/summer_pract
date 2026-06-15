package org.client.menu.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.client.MainGame;
import org.client.LanguageUI;

public class LanguageSelectorItem extends SettingsItem {
    private final String titleUa, titleEn, descUa, descEn;
    private final float btnW = 150, btnH = 45;
    private final MainGame mainGame;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color ACCENT_GOLD = new Color(0xffd700ff);

    public LanguageSelectorItem(MainGame mainGame, String tUa, String tEn, String dUa, String dEn) {
        this.mainGame = mainGame;
        this.titleUa = tUa; this.titleEn = tEn;
        this.descUa = dUa; this.descEn = dEn;
    }

    @Override public float getHeight() { return 150; }

    @Override
    public void handleInput(float mx, float my, boolean jt, boolean it, float startX, float startY) {
        if (!jt) return;
        float btnY = startY - 130;
        float enX = startX + btnW + 20;

        if (mx >= startX && mx <= startX + btnW && my >= btnY && my <= btnY + btnH) {
            mainGame.setCurrentLanguage(LanguageUI.UKRAINIAN);
        }
        if (mx >= enX && mx <= enX + btnW && my >= btnY && my <= btnY + btnH) {
            mainGame.setCurrentLanguage(LanguageUI.ENGLISH);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float startX, float startY, boolean isUa) {
        float titleY = startY - 25, descY = startY - 60, btnY = startY - 130;
        float enX = startX + btnW + 20;

        batch.begin();
        fontL.setColor(Color.WHITE); fontL.draw(batch, isUa ? titleUa : titleEn, startX, titleY);
        fontS.setColor(Color.GRAY); fontS.draw(batch, isUa ? descUa : descEn, startX, descY);
        batch.end();

        boolean isCurrentUa = mainGame.getCurrentLanguage() == LanguageUI.UKRAINIAN;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        SettingsMenu.drawRoundRectStatic(shapes, startX, btnY, btnW, btnH, 8, isCurrentUa ? ACCENT_GOLD : new Color(0x3a3f52ff));
        SettingsMenu.drawRoundRectStatic(shapes, enX, btnY, btnW, btnH, 8, !isCurrentUa ? ACCENT_GOLD : new Color(0x3a3f52ff));
        shapes.end();

        batch.begin();
        fontM.setColor(isCurrentUa ? Color.BLACK : Color.WHITE);
        layout.setText(fontM, "Українська");
        fontM.draw(batch, "Українська", startX + (btnW - layout.width)/2f, btnY + (btnH - layout.height)/2f + layout.height);

        fontM.setColor(!isCurrentUa ? Color.BLACK : Color.WHITE);
        layout.setText(fontM, "English");
        fontM.draw(batch, "English", enX + (btnW - layout.width)/2f, btnY + (btnH - layout.height)/2f + layout.height);
        batch.end();
    }
}
