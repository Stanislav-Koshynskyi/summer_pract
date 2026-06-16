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
import org.core.enums.LevelState;
import org.client.MenuStatus;
import org.client.LanguageUI;

public class SelectLevelMenu implements Screen {

    private static final Color BG_COLOR      = Color.valueOf("12121aff");
    private static final Color SURFACE       = Color.valueOf("1e1e2eff");
    private static final Color SURFACE2      = Color.valueOf("2a2a3eff");
    private static final Color SURFACE_HOVER = Color.valueOf("32324aff");
    private static final Color BORDER        = Color.valueOf("3a3a5cff");
    private static final Color GOLD          = Color.valueOf("f5c842ff");
    private static final Color GREEN         = Color.valueOf("4ecb71ff");
    private static final Color RED           = Color.valueOf("e05252ff");
    private static final Color BLUE          = Color.valueOf("5ea8ffff");
    private static final Color WHITE         = Color.valueOf("eeeef5ff");
    private static final Color DIM           = Color.valueOf("aaaaaaff");
    private static final Color LOCKED_COLOR  = Color.valueOf("555555ff");

    private static final float WORLD_W   = 1280f;
    private static final float WORLD_H   = 720f;
    private static final int   RADIUS    = 12;


    private static final float CARD_W    = 200f;
    private static final float CARD_H    = 240f;
    private static final float CARD_GAP  = 60f;


    private static final float BACK_BTN_W = 220f;
    private static final float BACK_BTN_H = 60f;
    private static final float BACK_BTN_X = (WORLD_W - BACK_BTN_W) / 2f;
    private static final float BACK_BTN_Y = 50f;

    private final com.badlogic.gdx.Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final GlyphLayout layout;


    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont numberFont;
    private BitmapFont labelFont;

    private final SwitchMenu switchMenu;
    private final LevelData[] levels;

    public static final int LEVEL_NUMBER = 3;

    private static class LevelData {
        String id;
        LevelState state;
        String labelUa;
        String labelEn;
        int stars;
        float x, y;
/** Дані рівня*/
        LevelData(String id, LevelState state, String labelUa, String labelEn, int stars) {
            this.id = id;
            this.state = state;
            this.labelUa = labelUa;
            this.labelEn = labelEn;
            this.stars = stars;
        }
    }

    public SelectLevelMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        shapes.setAutoShapeType(true);
        batch  = new SpriteBatch();
        layout = new GlyphLayout();

        generateFonts();

        int maxUnlocked = ((MainGame) game).getMaxUnlockedLevel();

        // Рівні
        levels = new LevelData[] {
                new LevelData("01", maxUnlocked > 1 ? LevelState.DONE : LevelState.ACTIVE, "", "", 2),
                new LevelData("02", maxUnlocked > 2 ? LevelState.DONE : (maxUnlocked == 2 ? LevelState.ACTIVE : LevelState.LOCKED), "", "", 0),
                new LevelData("03", maxUnlocked > 3 ? LevelState.DONE : (maxUnlocked == 3 ? LevelState.ACTIVE : LevelState.LOCKED), "", "", 0)
        };

        // Розрахунок позицій карток (центрування ряду)
        float totalWidth = (levels.length * CARD_W) + ((levels.length - 1) * CARD_GAP);
        float startX = (WORLD_W - totalWidth) / 2f;
        float cardY = (WORLD_H - CARD_H) / 2f + 20f;

        for (int i = 0; i < levels.length; i++) {
            levels[i].x = startX + i * (CARD_W + CARD_GAP);
            levels[i].y = cardY;
        }

        ((MainGame) game).playMenuMusic();
    }
    /**шрифти*/
    private void generateFonts() {
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            parameter.size = 48;
            titleFont = generator.generateFont(parameter);
            titleFont.setColor(GOLD);

            parameter.size = 20;
            subtitleFont = generator.generateFont(parameter);
            subtitleFont.setColor(DIM);

            parameter.size = 64;
            numberFont = generator.generateFont(parameter);
            numberFont.setColor(WHITE);

            parameter.size = 18;
            labelFont = generator.generateFont(parameter);

            generator.dispose();
        } else {

            titleFont = new BitmapFont(); subtitleFont = new BitmapFont();
            numberFont = new BitmapFont(); labelFont = new BitmapFont();
            Gdx.app.error("FontError", "Шрифт не знайдено!");
        }
    }

    @Override public void show() {}
    /**Малювання екрану*/
    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        if (game.getScreen() != this) return;

        camera.update();

        drawShapes();
        drawText();
        handleInput();
    }
    /**Малювання рівня*/
    private void drawShapes() {
        shapes.setProjectionMatrix(camera.combined);

        //  Малювання з'єднувальних ліній між картками
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < levels.length - 1; i++) {
            float lineX = levels[i].x + CARD_W;
            float lineY = levels[i].y + CARD_H / 2f;
            shapes.setColor(levels[i].state == LevelState.DONE ? GOLD : BORDER);
            shapes.rect(lineX, lineY - 2, CARD_GAP, 4);
        }
        shapes.end();

        //  Малювання фону карток та кнопки "Назад"
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (LevelData level : levels) {
            boolean hovered = isMouseOver(level.x, level.y, CARD_W, CARD_H) && level.state != LevelState.LOCKED;
            Color bgColor = hovered ? SURFACE_HOVER : SURFACE2;
            drawRoundRect(level.x, level.y, CARD_W, CARD_H, RADIUS, bgColor);

        }

        // Кнопка Назад
        boolean backHovered = isMouseOver(BACK_BTN_X, BACK_BTN_Y, BACK_BTN_W, BACK_BTN_H);
        drawRoundRect(BACK_BTN_X, BACK_BTN_Y, BACK_BTN_W, BACK_BTN_H, RADIUS, backHovered ? SURFACE_HOVER : SURFACE2);
        shapes.end();

        //  Малювання обводок карток (Border)
        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3f);
        for (LevelData level : levels) {
            Color borderColor = BORDER;
            if (level.state == LevelState.DONE) borderColor = GOLD;
            else if (level.state == LevelState.ACTIVE) borderColor = GREEN;

            shapes.setColor(borderColor);
            drawRoundRectOutline(level.x, level.y, CARD_W, CARD_H, RADIUS);

        }

        shapes.setColor(BORDER);
        drawRoundRectOutline(BACK_BTN_X, BACK_BTN_Y, BACK_BTN_W, BACK_BTN_H, RADIUS);
        shapes.end();
        Gdx.gl.glLineWidth(1f);
    }
    /**Малювання тексту*/
    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;

        // Заголовки
        String title = isUa ? "ВИБІР РІВНЯ" : "SELECT STAGE";

        layout.setText(titleFont, title);
        titleFont.draw(batch, title, (WORLD_W - layout.width) / 2f, WORLD_H - 60f);



        for (LevelData level : levels) {
            // Номер рівня
            String numStr = level.id;
            numberFont.setColor(level.state == LevelState.LOCKED ? LOCKED_COLOR : WHITE);
            layout.setText(numberFont, numStr);
            float numY = level.y + (CARD_H / 1.75f) + (layout.height / 1.75f) - 10f;
            numberFont.draw(batch, numStr, level.x + (CARD_W - layout.width) / 2f, numY);

            // Назва рівня (під карткою)
            String label = isUa ? level.labelUa : level.labelEn;
            labelFont.setColor(level.state == LevelState.DONE ? GOLD : (level.state == LevelState.LOCKED ? LOCKED_COLOR : DIM));
            layout.setText(labelFont, label);
            labelFont.draw(batch, label, level.x + (CARD_W - layout.width) / 2f, level.y - 25f);

            // Статус рівня (над назвою)
            String stateStr = "";
            if (level.state == LevelState.DONE) { stateStr = isUa ? "ЗАВЕРШЕНО" : "COMPLETED"; labelFont.setColor(GOLD); }
            else if (level.state == LevelState.ACTIVE) { stateStr = isUa ? "ДОСТУПНО" : "AVAILABLE"; labelFont.setColor(GREEN); }
            else { stateStr = isUa ? "ЗАБЛОКОВАНО" : "LOCKED"; labelFont.setColor(LOCKED_COLOR); }

            layout.setText(labelFont, stateStr);
            labelFont.draw(batch, stateStr, level.x + (CARD_W - layout.width) / 2f, level.y - 5f);
        }

        // Кнопка Назад
        String backTxt = isUa ? "← НАЗАД" : "← BACK";
        labelFont.setColor(DIM);
        layout.setText(labelFont, backTxt);
        labelFont.draw(batch, backTxt, BACK_BTN_X + (BACK_BTN_W - layout.width) / 2f, BACK_BTN_Y + (BACK_BTN_H + layout.height) / 2f);

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

        // Клік по кнопці "Назад"
        if (wx >= BACK_BTN_X && wx <= BACK_BTN_X + BACK_BTN_W && wy >= BACK_BTN_Y && wy <= BACK_BTN_Y + BACK_BTN_H) {
            switchMenu.switchMenu(MenuStatus.START_MENU);
            return;
        }

        // Клік по картках рівнів
        for (LevelData level : levels) {
            if (wx >= level.x && wx <= level.x + CARD_W && wy >= level.y && wy <= level.y + CARD_H) {
                if (level.state != LevelState.LOCKED) {
                    System.out.println("Завантаження рівня: " + level.id);
                    ((MainGame)game).setCurrentLevel(Integer.parseInt(level.id));
                    switchMenu.switchMenu(MenuStatus.PLAY_GAME_MENU);
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

    // Допоміжні методи малювання заокруглених прямокутників
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

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        shapes.dispose();
        batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (subtitleFont != null) subtitleFont.dispose();
        if (numberFont != null) numberFont.dispose();
        if (labelFont != null) labelFont.dispose();
    }
}
