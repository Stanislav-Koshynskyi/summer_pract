package org.client.menu.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

public class KeyboardPage implements SettingsPage {
    private final String tabNameUa, tabNameEn;

    // Кольори категорій
    private final Color COL_MOVE = new Color(0x4a90e2ff);    // Синій (Рух)
    private final Color COL_STEALTH = new Color(0x50e3c2ff); // Зелений (Стелс)
    private final Color COL_ACTION = new Color(0xffd700ff);  // Жовтий (Дії/Зброя)
    private final Color COL_SYS = new Color(0xe02020ff);     // Червоний (Система)
    private final Color COL_DEFAULT = new Color(0x2d3142ff); // Базовий
    private final Color COL_BG = new Color(0x1a1c26ff);      // Фон клавіші

    private final List<KeyDef> keys = new ArrayList<>();
    private KeyDef selectedKey = null;
    private final GlyphLayout layout = new GlyphLayout();

    public KeyboardPage(String ua, String en) {
        this.tabNameUa = ua;
        this.tabNameEn = en;
        initKeyboardLayout();

        // За замовчуванням обираємо першу активну клавішу
        if (!keys.isEmpty()) selectedKey = keys.get(0);
    }

    @Override public String getName(boolean isUa) { return isUa ? tabNameUa : tabNameEn; }

    private void initKeyboardLayout() {
        float startX = 300; // Відступ зліва
        float startY = 550; // Відступ зверху
        float keySize = 45;
        float gap = 8;

        // --- РЯДОК 1 (Esc, F-клавіші) ---
        addKey("ESC", startX, startY, keySize * 1.5f, COL_SYS, "ESC - Меню", "ESC - Menu", "Відкрити головне меню або скасувати дію.", "Open the main menu or cancel an action.");
        // Можна додати F1, F2... за потребою

        startY -= (keySize + gap);
        // --- РЯДОК 2 (Цифри) ---
        float curX = startX;
        addKey("~", curX, startY, keySize, COL_DEFAULT, "", "", "", ""); curX += keySize + gap;
        addKey("1", curX, startY, keySize, COL_ACTION, "1 - Зброя 1", "1 - Weapon 1", "Екіпірувати основну зброю.", "Equip primary weapon."); curX += keySize + gap;
        addKey("2", curX, startY, keySize, COL_ACTION, "2 - Зброя 2", "2 - Weapon 2", "Екіпірувати вторинну зброю.", "Equip secondary weapon."); curX += keySize + gap;
        addKey("3", curX, startY, keySize, COL_ACTION, "3 - Аптечка", "3 - Medkit", "Використати аптечку.", "Use a medkit."); curX += keySize + gap;
        addKey("4", curX, startY, keySize, COL_ACTION, "4 - Граната", "4 - Grenade", "Підготувати гранату.", "Equip grenade.");

        startY -= (keySize + gap);
        // --- РЯДОК 3 (Tab, Q, W, E, R...) ---
        curX = startX;
        addKey("Tab", curX, startY, keySize * 1.5f, COL_SYS, "Tab - Інвентар", "Tab - Inventory", "Відкрити рюкзак.", "Open backpack."); curX += keySize * 1.5f + gap;
        addKey("Q", curX, startY, keySize, COL_ACTION, "Q - Нахил вліво", "Q - Lean Left", "Визирнути з-за укриття вліво.", "Lean left from cover."); curX += keySize + gap;
        addKey("W", curX, startY, keySize, COL_MOVE, "W - Вперед", "W - Move Forward", "Рух персонажа вперед.", "Move character forward."); curX += keySize + gap;
        addKey("E", curX, startY, keySize, COL_ACTION, "E - Взаємодія", "E - Interact", "Взаємодія з об'єктами: відчиняє двері, підбирає предмети.", "Interact with objects: open doors, pick up items."); curX += keySize + gap;
        addKey("R", curX, startY, keySize, COL_ACTION, "R - Перезарядка", "R - Reload", "Перезарядити поточну зброю.", "Reload current weapon.");

        startY -= (keySize + gap);
        // --- РЯДОК 4 (Caps, A, S, D, F...) ---
        curX = startX;
        addKey("Caps", curX, startY, keySize * 1.8f, COL_DEFAULT, "", "", "", ""); curX += keySize * 1.8f + gap;
        addKey("A", curX, startY, keySize, COL_MOVE, "A - Вліво", "A - Move Left", "Рух персонажа вліво.", "Move character left."); curX += keySize + gap;
        addKey("S", curX, startY, keySize, COL_MOVE, "S - Назад", "S - Move Backward", "Рух персонажа назад.", "Move character backward."); curX += keySize + gap;
        addKey("D", curX, startY, keySize, COL_MOVE, "D - Вправо", "D - Move Right", "Рух персонажа вправо.", "Move character right."); curX += keySize + gap;
        addKey("F", curX, startY, keySize, COL_ACTION, "F - Ліхтарик", "F - Flashlight", "Увімкнути або вимкнути ліхтарик.", "Toggle flashlight.");

        startY -= (keySize + gap);
        // --- РЯДОК 5 (Shift, Z, X, C...) ---
        curX = startX;
        addKey("Shift", curX, startY, keySize * 2.2f, COL_STEALTH, "Shift - Біг", "Shift - Sprint", "Пришвидшити рух персонажа.", "Sprint to move faster."); curX += keySize * 2.2f + gap;
        addKey("Z", curX, startY, keySize, COL_DEFAULT, "", "", "", ""); curX += keySize + gap;
        addKey("X", curX, startY, keySize, COL_DEFAULT, "", "", "", ""); curX += keySize + gap;
        addKey("C", curX, startY, keySize, COL_STEALTH, "C - Присісти", "C - Crouch", "Присісти для зменшення шуму.", "Crouch to reduce noise."); curX += keySize + gap;
        addKey("V", curX, startY, keySize, COL_ACTION, "V - Ближній бій", "V - Melee", "Удар ножем.", "Melee knife attack.");

        startY -= (keySize + gap);
        // --- РЯДОК 6 (Ctrl, Space...) ---
        curX = startX;
        addKey("Ctrl", curX, startY, keySize * 1.5f, COL_STEALTH, "Ctrl - Лягти", "Ctrl - Prone", "Лягти на землю.", "Go prone."); curX += keySize * 1.5f + gap;
        addKey("Alt", curX, startY, keySize * 1.2f, COL_DEFAULT, "", "", "", ""); curX += keySize * 1.2f + gap;
        addKey("Space", curX, startY, keySize * 5f, COL_MOVE, "Space - Стрибок", "Space - Jump", "Стрибок через перешкоди.", "Jump over obstacles.");
    }

    private void addKey(String lbl, float x, float y, float w, Color c, String nUa, String nEn, String dUa, String dEn) {
        keys.add(new KeyDef(lbl, x, y, w, 45, c, nUa, nEn, dUa, dEn));
    }

    @Override
    public void handleInput(float mx, float my, boolean jt, boolean it) {
        if (jt) {
            for (KeyDef k : keys) {
                if (mx >= k.x && mx <= k.x + k.w && my >= k.y && my <= k.y + k.h) {
                    // Якщо клавіша має опис, обираємо її
                    if (!k.nameUa.isEmpty()) {
                        selectedKey = k;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float sidebarW, float topBarH, boolean isUa) {
        // 1. Малюємо рамку клавіатури (опціонально, як на дизайні)
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_DEFAULT);
        shapes.rect(sidebarW + 20, 150, 1280 - sidebarW - 350, 720 - topBarH - 200);
        shapes.end();

        // 2. Малюємо клавіші
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (KeyDef k : keys) {
            boolean isSelected = (k == selectedKey);

            // Фон клавіші
            SettingsMenu.drawRoundRectStatic(shapes, k.x, k.y, k.w, k.h, 6, isSelected ? new Color(0x2a2c36ff) : COL_BG);
        }
        shapes.end();

        // Обводка клавіш (Кольорова)
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (KeyDef k : keys) {
            shapes.setColor(k.color);
            // Спрощена обводка (без заокруглень для лінії, або використовуємо прямокутник)
            shapes.rect(k.x, k.y, k.w, k.h);
        }
        shapes.end();

        // Текст на клавішах
        batch.begin();
        for (KeyDef k : keys) {
            fontS.setColor(k.color == COL_DEFAULT ? Color.GRAY : k.color);
            layout.setText(fontS, k.label);
            fontS.draw(batch, k.label, k.x + (k.w - layout.width) / 2f, k.y + (k.h + layout.height) / 2f);
        }

        // 3. Легенда (внизу зліва)
        float legX = sidebarW + 20;
        float legY = 100;
        drawLegendItem(batch, shapes, fontS, legX, legY, COL_MOVE, isUa ? "РУХ" : "MOVE"); legX += 100;
        drawLegendItem(batch, shapes, fontS, legX, legY, COL_STEALTH, isUa ? "СТЕЛС" : "STEALTH"); legX += 120;
        drawLegendItem(batch, shapes, fontS, legX, legY, COL_ACTION, isUa ? "ДІЇ / ЗБРОЯ" : "ACTION"); legX += 160;
        drawLegendItem(batch, shapes, fontS, legX, legY, COL_SYS, isUa ? "СИСТЕМА" : "SYSTEM");

        // 4. Права панель (Деталі обраної клавіші)
        float panelX = 1280 - 300;
        if (selectedKey != null && !selectedKey.nameUa.isEmpty()) {
            fontM.setColor(selectedKey.color);
            fontM.draw(batch, isUa ? selectedKey.nameUa : selectedKey.nameEn, panelX, 600);

            fontS.setColor(Color.LIGHT_GRAY);
            String desc = isUa ? selectedKey.descUa : selectedKey.descEn;
            layout.setText(fontS, desc, Color.LIGHT_GRAY, 280, Align.left, true);
            fontS.draw(batch, desc, panelX, 550, 280, Align.left, true);
        } else {
            fontS.setColor(Color.GRAY);
            fontS.draw(batch, isUa ? "Натисніть клавішу для деталей" : "Click a key for details", panelX, 600);
        }
        batch.end();

        // Лінія відділення правої панелі
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_DEFAULT);
        shapes.rect(panelX - 20, 0, 1, 720 - topBarH);
        shapes.end();
    }

    private void drawLegendItem(SpriteBatch batch, ShapeRenderer shapes, BitmapFont font, float x, float y, Color c, String text) {
        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(c);
        shapes.rect(x, y, 15, 15);
        shapes.end();
        batch.begin();
        font.setColor(Color.GRAY);
        font.draw(batch, text, x + 25, y + 13);
    }

    private static class KeyDef {
        String label, nameUa, nameEn, descUa, descEn;
        float x, y, w, h;
        Color color;

        KeyDef(String l, float x, float y, float w, float h, Color c, String nUa, String nEn, String dUa, String dEn) {
            this.label = l; this.x = x; this.y = y; this.w = w; this.h = h; this.color = c;
            this.nameUa = nUa; this.nameEn = nEn; this.descUa = dUa; this.descEn = dEn;
        }
    }
}
