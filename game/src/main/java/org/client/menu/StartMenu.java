package org.client.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.client.MainGame;
import org.client.LanguageUI;
import org.client.MenuStatus;

public class StartMenu implements Screen {

    private static final Color BG_COLOR = new Color(0x2e3240ff);
    private static final Color BTN_COLOR = new Color(0x3a3f52ff);
    private static final Color BTN_BORDER = new Color(0x6b7280ff);
    private static final Color BTN_HOVER = new Color(0x4a5068ff);

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final float BTN_W = 440f;
    private static final float BTN_H = 80f;
    private static final float BTN_GAP = 20f;
    private static final float BORDER = 3f;
    private static final int RADIUS = 18;

    private final float exitBtnW = 60f;
    private final float exitBtnH = 60f;
    private final float exitBtnX = WORLD_W - exitBtnW - 40f;
    private final float exitBtnY = WORLD_H - exitBtnH - 40f;

    // Динамічний масив підписів кнопок
    private final String[] labels;

    // Кнопка перемикання мов справа внизу
    private final float langBtnW = 160f;
    private final float langBtnH = 60f;
    private final float langBtnX = WORLD_W - langBtnW - 40f;
    private final float langBtnY = 40f;

    private final com.badlogic.gdx.Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final SpriteBatch batch;
    private final GlyphLayout layout;

    private final float[] btnX;
    private final float[] btnY;

    private static final MenuStatus[] BUTTON_TARGETS = {
            MenuStatus.SELECT_PLAYER_MENU,
            MenuStatus.ABOUT_AUTHORS_MENU,
            MenuStatus.SETTINGS_MENU,
            MenuStatus.RULES_MENU
    };

    private final SwitchMenu switchMenu;

    public StartMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;

        // Визначаємо тексти кнопок залежно від обраної мови гри
        MainGame mainGame = (MainGame) game;
        if (mainGame.getCurrentLanguage() == LanguageUI.ENGLISH) {
            labels = new String[]{"Start Game", "About Authors", "Settings", "Rules"};
        } else {
            labels = new String[]{"Почати гру", "Про авторів", "Налаштування", "Правила"};
        }

        btnX = new float[labels.length];
        btnY = new float[labels.length];

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            parameter.size = 70;
            titleFont = generator.generateFont(parameter);
            titleFont.setColor(Color.WHITE);

            parameter.size = 32;
            font = generator.generateFont(parameter);
            font.setColor(Color.WHITE);

            generator.dispose();
        } else {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(5f);
            font = new BitmapFont();
            font.getData().setScale(2.8f);
            Gdx.app.error("FontError", "Шрифт не знайдено!");
        }

        float totalHeight = labels.length * BTN_H + (labels.length - 1) * BTN_GAP;
        float startY = (WORLD_H - totalHeight) / 2f - 30f;

        for (int i = 0; i < labels.length; i++) {
            btnX[i] = (WORLD_W - BTN_W) / 2f;
            btnY[i] = startY + (labels.length - 1 - i) * (BTN_H + BTN_GAP);
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        if (game.getScreen() != this) return;

        camera.update();

        drawButtons();
        drawText();
        handleInput();
    }

    private void drawButtons() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Малювання основних кнопок меню
        for (int i = 0; i < labels.length; i++) {
            boolean hovered = isMouseOver(i);
            drawRoundRect(btnX[i], btnY[i], BTN_W, BTN_H, RADIUS, hovered ? BTN_HOVER : BTN_COLOR);
        }

        // Малювання кнопки мови
        boolean langHovered = isMouseOverLang();
        drawRoundRect(langBtnX, langBtnY, langBtnW, langBtnH, RADIUS, langHovered ? BTN_HOVER : BTN_COLOR);
        boolean exitHovered = isMouseOverExit();
        drawRoundRect(exitBtnX, exitBtnY, exitBtnW, exitBtnH, RADIUS, exitHovered ? BTN_HOVER : BTN_COLOR);

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BTN_BORDER);
        Gdx.gl.glLineWidth(BORDER);

        for (int i = 0; i < labels.length; i++) {
            drawRoundRectOutline(btnX[i], btnY[i], BTN_W, BTN_H, RADIUS);
        }

        // Обводка кнопки мови
        drawRoundRectOutline(langBtnX, langBtnY, langBtnW, langBtnH, RADIUS);

        drawRoundRectOutline(exitBtnX, exitBtnY, exitBtnW, exitBtnH, RADIUS);


        shapes.end();
        Gdx.gl.glLineWidth(1f);

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

    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String title = "One By One";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, (WORLD_W - layout.width) / 2f, btnY[0] + BTN_H + layout.height + 60f);

        for (int i = 0; i < labels.length; i++) {
            layout.setText(font, labels[i]);
            font.draw(batch, labels[i], btnX[i] + (BTN_W - layout.width) / 2f, btnY[i] + (BTN_H + layout.height) / 2f);
        }

        String exitText = "X";
        layout.setText(font, exitText);
        font.draw(batch, exitText, exitBtnX + (exitBtnW - layout.width) / 2f, exitBtnY + (exitBtnH + layout.height) / 2f);

        // Відображення поточної мови на кнопці (UA або EN)
        MainGame mainGame = (MainGame) game;
        String langText = mainGame.getCurrentLanguage() == LanguageUI.ENGLISH ? "EN" : "UA";
        layout.setText(font, langText);
        font.draw(batch, langText, langBtnX + (langBtnW - layout.width) / 2f, langBtnY + (langBtnH + layout.height) / 2f);

        batch.end();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;

        // Клік по кнопці мови
        if (wx >= langBtnX && wx <= langBtnX + langBtnW && wy >= langBtnY && wy <= langBtnY + langBtnH) {
            MainGame mainGame = (MainGame) game;
            if (mainGame.getCurrentLanguage() == LanguageUI.ENGLISH) {
                mainGame.setCurrentLanguage(LanguageUI.UKRAINIAN);
            } else {
                mainGame.setCurrentLanguage(LanguageUI.ENGLISH);
            }
            // Перезапускаємо екран меню для миттєвого оновлення
            switchMenu.switchMenu(MenuStatus.START_MENU);
            return;
        }
        // вихід
        if (wx >= exitBtnX && wx <= exitBtnX + exitBtnW && wy >= exitBtnY && wy <= exitBtnY + exitBtnH) {
            Gdx.app.exit();
            return;
        }

        for (int i = 0; i < labels.length; i++) {
            if (wx >= btnX[i] && wx <= btnX[i] + BTN_W && wy >= btnY[i] && wy <= btnY[i] + BTN_H) {
                onButtonClick(i);
                return;
            }
        }
    }

    private void onButtonClick(int index) {
        if (index >= 0 && index < BUTTON_TARGETS.length) {
            MenuStatus target = BUTTON_TARGETS[index];

            // Якщо гравець натиснув "Грати"
            if (target == MenuStatus.SELECT_PLAYER_MENU) {
                MainGame mainGame = (MainGame) game;

                // Якщо розблоковано лише 1 рівень — відправляємо в інтро
                if (mainGame.getMaxUnlockedLevel() <= 1) {
                    switchMenu.switchMenu(MenuStatus.INTRO_MENU);
                } else {
                    // Якщо розблоковано 2 і більше — показуємо карту рівнів
                    switchMenu.switchMenu(MenuStatus.SELECT_PLAYER_MENU);
                }
            } else {
                // Для всіх інших кнопок (налаштування, автори тощо)
                switchMenu.pushScreen(target);
            }
        }
    }

    private boolean isMouseOver(int i) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;
        return wx >= btnX[i] && wx <= btnX[i] + BTN_W && wy >= btnY[i] && wy <= btnY[i] + BTN_H;
    }

    private boolean isMouseOverLang() {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;
        return wx >= langBtnX && wx <= langBtnX + langBtnW && wy >= langBtnY && wy <= langBtnY + langBtnH;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        titleFont.dispose();
    }
    private boolean isMouseOverExit() {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;
        return wx >= exitBtnX && wx <= exitBtnX + exitBtnW && wy >= exitBtnY && wy <= exitBtnY + exitBtnH;
    }
}