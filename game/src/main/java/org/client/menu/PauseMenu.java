package org.client.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.tools.javac.Main;
import org.client.MainGame;
import org.core.enums.LanguageUI;
import org.core.enums.MenuStatus;

public class PauseMenu implements Screen {

    // Кольори
    private static final Color OVERLAY_COLOR = new Color(0x2e3240ff); // Затемнення фону
    private static final Color PANEL_BG      = Color.valueOf("12121aff");
    private static final Color PANEL_BORDER  = Color.valueOf("#43b02b");
    private static final Color BTN_COLOR     = Color.valueOf("1e1e2eff");
    private static final Color BTN_BORDER    = Color.valueOf("3a3a5cff");
    private static final Color BTN_HOVER     = Color.valueOf("32324aff");
    private static final Color GOLD          = Color.valueOf("f5c842ff");

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Геометрія панелі
    private static final float PANEL_W = 400f;
    private static final float PANEL_H = 550f;

    // Геометрія кнопок
    private static final float BTN_W = 320f;
    private static final float BTN_H = 60f;
    private static final float BTN_GAP = 15f;
    private static final int RADIUS = 12;

    private final com.badlogic.gdx.Game game;
    private final SwitchMenu switchMenu;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final GlyphLayout layout;

    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont levelFont;

    private final String[] labels;
    private final float[] btnY;
    private final float panelX, panelY;
    private final float btnX;

    // Статуси для кнопок
    private static final MenuStatus[] TARGETS = {
            MenuStatus.PLAY_GAME_MENU,      // Продовжити
            MenuStatus.SETTINGS_MENU,       // Налаштування
            MenuStatus.RULES_MENU,          // Правила
            MenuStatus.START_MENU           // Головне меню
    };

    public PauseMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;

        MainGame mainGame = (MainGame) game;
        boolean isEn = mainGame.getCurrentLanguage() == LanguageUI.ENGLISH;

        labels = isEn ?
                new String[]{"Resume", "Settings", "Rules", "Main Menu"} :
                new String[]{"Продовжити", "Налаштування", "Правила", "Головне меню"};

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        shapes.setAutoShapeType(true);
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        generateFonts();

        // Розрахунок позицій
        panelX = (WORLD_W - PANEL_W) / 2f;
        panelY = (WORLD_H - PANEL_H) / 2f;
        btnX = (WORLD_W - BTN_W) / 2f;

        btnY = new float[labels.length];
        float startY = panelY + 60f;
        for (int i = 0; i < labels.length; i++) {
            btnY[i] = startY + (labels.length - 1 - i) * (BTN_H + BTN_GAP);
        }
    }

    private void generateFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

        parameter.size = 50;
        titleFont = generator.generateFont(parameter);
        titleFont.setColor(GOLD);

        parameter.size = 35;
        levelFont = generator.generateFont(parameter);
        levelFont.setColor(Color.WHITE);

        parameter.size = 22;
        font = generator.generateFont(parameter);

        generator.dispose();
    }

    @Override
    public void render(float delta) {
        // Ми не робимо ScreenUtils.clear, щоб бачити гру на фоні (якщо SwitchMenu дозволяє)
        // Але оскільки ваше SwitchMenu видаляє попередній екран, ми малюємо темний фон
        ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1f);

        camera.update();

        // 1. Малюємо затемнення всього екрану
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(OVERLAY_COLOR);
        shapes.rect(0, 0, WORLD_W, WORLD_H);

        // 2. Основна панель меню
        drawRoundRect(panelX, panelY, PANEL_W, PANEL_H, RADIUS, PANEL_BG);
        shapes.end();

        // Обводка панелі
        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3f);
        shapes.setColor(PANEL_BORDER);
        drawRoundRectOutline(panelX, panelY, PANEL_W, PANEL_H, RADIUS);
        shapes.end();

        // 3. Кнопки
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < labels.length; i++) {
            boolean hovered = isMouseOver(btnX, btnY[i], BTN_W, BTN_H);
            drawRoundRect(btnX, btnY[i], BTN_W, BTN_H, RADIUS, hovered ? BTN_HOVER : BTN_COLOR);
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        shapes.setColor(BTN_BORDER);
        for (int i = 0; i < labels.length; i++) {
            drawRoundRectOutline(btnX, btnY[i], BTN_W, BTN_H, RADIUS);
        }
        shapes.end();

        // 4. Текст
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Текст заголовку
        String t1 = "ПАУЗА";
        layout.setText(titleFont, t1);
        titleFont.draw(batch, t1, (WORLD_W - layout.width) / 2f, panelY + PANEL_H - 40f);

        String t2 = "РІВЕНЬ "+ ((MainGame)game).getCurrentLevel(); // Можна зробити динамічним
        layout.setText(levelFont, t2);
        levelFont.draw(batch, t2, (WORLD_W - layout.width) / 2f, panelY + PANEL_H - 120f);

        // Текст кнопок
        for (int i = 0; i < labels.length; i++) {
            font.setColor(isMouseOver(btnX, btnY[i], BTN_W, BTN_H) ? GOLD : Color.WHITE);
            layout.setText(font, labels[i]);
            font.draw(batch, labels[i], btnX + (BTN_W - layout.width) / 2f, btnY[i] + (BTN_H + layout.height) / 2f);
        }
        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;

        for (int i = 0; i < labels.length; i++) {
            if (wx >= btnX && wx <= btnX + BTN_W && wy >= btnY[i] && wy <= btnY[i] + BTN_H) {
                if (i < TARGETS.length) {
                    if (i == 3) {
                        switchMenu.switchMenu(TARGETS[i]);
                        switchMenu.clearStack();
                    }
                    else if (i == 0){
                        switchMenu.popScreen();
                    }
                    else {
                        switchMenu.pushScreen(TARGETS[i]);
                    }
                }
                return;
            }
        }
    }

    private boolean isMouseOver(float x, float y, float w, float h) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;
        return wx >= x && wx <= x + w && wy >= y && wy <= y + h;
    }

    private void drawRoundRect(float x, float y, float w, float h, int r, Color color) {
        shapes.setColor(color);
        shapes.rect(x + r, y, w - 2 * r, h);
        shapes.rect(x, y + r, r, h - 2 * r);
        shapes.rect(x + w - r, y + r, r, h - 2 * r);
        shapes.circle(x + r, y + r, r, 20);
        shapes.circle(x + w - r, y + r, r, 20);
        shapes.circle(x + r, y + h - r, r, 20);
        shapes.circle(x + w - r, y + h - r, r, 20);
    }

    private void drawRoundRectOutline(float x, float y, float w, float h, int r) {
        shapes.line(x + r, y, x + w - r, y);
        shapes.line(x + r, y + h, x + w - r, y + h);
        shapes.line(x, y + r, x, y + h - r);
        shapes.line(x + w, y + r, x + w, y + h - r);
        shapes.arc(x + r, y + r, r, 180, 90, 12);
        shapes.arc(x + w - r, y + r, r, 270, 90, 12);
        shapes.arc(x + r, y + h - r, r, 90, 90, 12);
        shapes.arc(x + w - r, y + h - r, r, 0, 90, 12);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
        levelFont.dispose();
    }
}