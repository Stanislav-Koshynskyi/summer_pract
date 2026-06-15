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
import org.core.enums.LanguageUI;
import org.core.enums.LevelOutcome;
import org.core.enums.MenuStatus;
import org.core.state.LevelStats;

public class LevelCompletedMenu implements Screen {

    private static final Color BG_COLOR = new Color(0x12121aff);
    private static final Color PANEL_BG = new Color(0x1e1e2eff);
    private static final Color GOLD = new Color(0xf5c842ff);
    private static final Color GREEN = new Color(0x4ecb71ff);
    private static final Color RED = new Color(0xe05252ff);
    private static final Color WHITE = new Color(0xeeeeffff);
    private static final Color DIM = new Color(0xaaaaaaff);

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final int RADIUS = 18;

    private final com.badlogic.gdx.Game game;
    private final SwitchMenu switchMenu;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final GlyphLayout layout;

    private BitmapFont titleFont;
    private BitmapFont scoreFont;
    private BitmapFont labelFont;

    private final LevelOutcome outcome;
    private final LevelStats stats;
    private final int finalScore;
    private final int nextLevel;  // -1, якщо немає наступного рівня

    private static final float BTN_W = 300f;
    private static final float BTN_H = 70f;
    private static final float BTN_GAP = 30f;

    public LevelCompletedMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu,
                              LevelOutcome outcome, LevelStats stats, int nextLevel) {
        this.game = game;
        this.switchMenu = switchMenu;
        this.outcome = outcome;
        this.stats = stats;
        this.finalScore = stats.calculateScore();
        this.nextLevel = nextLevel;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        initFonts();
    }

    private void initFonts() {
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            param.size = 52;
            titleFont = generator.generateFont(param);
            titleFont.setColor(GOLD);

            param.size = 42;
            scoreFont = generator.generateFont(param);
            scoreFont.setColor(WHITE);

            param.size = 24;
            labelFont = generator.generateFont(param);

            generator.dispose();
        } else {
            titleFont = new BitmapFont();
            scoreFont = new BitmapFont();
            labelFont = new BitmapFont();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        if (game.getScreen() != this) return;

        camera.update();
        drawShapes();
        drawText();
        handleInput();
    }

    private void drawShapes() {
        shapes.setProjectionMatrix(camera.combined);

        float panelW = 600f;
        float panelH = 500f;
        float panelX = (WORLD_W - panelW) / 2f;
        float panelY = (WORLD_H - panelH) / 2f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRoundRect(panelX, panelY, panelW, panelH, RADIUS, PANEL_BG);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3f);
        shapes.setColor(GOLD);
        drawRoundRectOutline(panelX, panelY, panelW, panelH, RADIUS);
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;

        boolean hasNextLevel = nextLevel > 0;
        float totalBtnW = hasNextLevel ? (BTN_W * 2 + BTN_GAP) : BTN_W;
        float startBtnX = (WORLD_W - totalBtnW) / 2f;
        float btnY = panelY - BTN_H - 20f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (hasNextLevel) {
            boolean nextHover = isMouseOver(startBtnX, btnY, BTN_W, BTN_H);
            drawRoundRect(startBtnX, btnY, BTN_W, BTN_H, RADIUS, nextHover ? GOLD : GREEN);
        }
        float menuBtnX = hasNextLevel ? startBtnX + BTN_W + BTN_GAP : startBtnX;
        boolean menuHover = isMouseOver(menuBtnX, btnY, BTN_W, BTN_H);
        drawRoundRect(menuBtnX, btnY, BTN_W, BTN_H, RADIUS, menuHover ? GOLD : RED);
        shapes.end();
    }

    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;

        float panelX = (WORLD_W - 600f) / 2f;
        float panelY = (WORLD_H - 500f) / 2f;
        float centerX = WORLD_W / 2f;

        String title = isUa ? "РІВЕНЬ ПРОЙДЕНО" : "LEVEL COMPLETE";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, centerX - layout.width / 2f, panelY + 450f);

        String outcomeText;
        Color outcomeColor;
        switch (outcome) {
            case FULL_STEALTH -> {
                outcomeText = isUa ? "БЕЗШУМНО" : "STEALTH";
                outcomeColor = GREEN;
            }
            case ASSAULT -> {
                outcomeText = isUa ? "ШТУРМ" : "ASSAULT";
                outcomeColor = RED;
            }
            default -> {
                outcomeText = isUa ? "ЗМІШАНО" : "MIXED";
                outcomeColor = GOLD;
            }
        }
        labelFont.setColor(outcomeColor);
        layout.setText(labelFont, outcomeText);
        labelFont.draw(batch, outcomeText, centerX - layout.width / 2f, panelY + 380f);

        labelFont.setColor(WHITE);
        float y = panelY + 320f;
        float lineH = 35f;

        String kills = (isUa ? "Вбивств: " : "Kills: ") + stats.getKillCount();
        String silent = (isUa ? "Тихих вбивств: " : "Silent kills: ") + stats.getSilentKillCount();
        String alerts = (isUa ? "Тривог: " : "Alerts: ") + stats.getUniqueEnemiesAlerted();
        String time = (isUa ? "Час: " : "Time: ") + String.format("%.1f", stats.getElapsedTime()) + "s";

        layout.setText(labelFont, kills);
        labelFont.draw(batch, kills, centerX - layout.width / 2f, y);
        y -= lineH;
        layout.setText(labelFont, silent);
        labelFont.draw(batch, silent, centerX - layout.width / 2f, y);
        y -= lineH;
        layout.setText(labelFont, alerts);
        labelFont.draw(batch, alerts, centerX - layout.width / 2f, y);
        y -= lineH;
        layout.setText(labelFont, time);
        labelFont.draw(batch, time, centerX - layout.width / 2f, y);
        y -= lineH * 1.5f;

        String scoreStr = (isUa ? "ОЧКИ: " : "SCORE: ") + finalScore;
        scoreFont.setColor(GOLD);
        layout.setText(scoreFont, scoreStr);
        scoreFont.draw(batch, scoreStr, centerX - layout.width / 2f, y);

        boolean hasNextLevel = nextLevel > 0;
        float totalBtnW = hasNextLevel ? (BTN_W * 2 + BTN_GAP) : BTN_W;
        float startBtnX = (WORLD_W - totalBtnW) / 2f;
        float btnY = panelY - BTN_H - 20f;

        labelFont.setColor(Color.BLACK);
        if (hasNextLevel) {
            String next = isUa ? "ДАЛІ" : "NEXT";
            layout.setText(labelFont, next);
            labelFont.draw(batch, next, startBtnX + (BTN_W - layout.width) / 2f, btnY + (BTN_H + layout.height) / 2f);
        }
        String menu = isUa ? "МЕНЮ" : "MENU";
        float menuBtnX = hasNextLevel ? startBtnX + BTN_W + BTN_GAP : startBtnX;
        layout.setText(labelFont, menu);
        labelFont.draw(batch, menu, menuBtnX + (BTN_W - layout.width) / 2f, btnY + (BTN_H + layout.height) / 2f);

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

        float panelY = (WORLD_H - 500f) / 2f;
        boolean hasNextLevel = nextLevel > 0;
        float totalBtnW = hasNextLevel ? (BTN_W * 2 + BTN_GAP) : BTN_W;
        float startBtnX = (WORLD_W - totalBtnW) / 2f;
        float btnY = panelY - BTN_H - 20f;

        if (hasNextLevel && wx >= startBtnX && wx <= startBtnX + BTN_W && wy >= btnY && wy <= btnY + BTN_H) {
            ((MainGame)game).setCurrentLevel(nextLevel);
            switchMenu.switchMenu(MenuStatus.PLAY_GAME_MENU);
            return;
        }

        float menuBtnX = hasNextLevel ? startBtnX + BTN_W + BTN_GAP : startBtnX;
        if (wx >= menuBtnX && wx <= menuBtnX + BTN_W && wy >= btnY && wy <= btnY + BTN_H) {
            switchMenu.switchMenu(MenuStatus.SELECT_LEVEL_MENU);
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
        titleFont.dispose();
        scoreFont.dispose();
        labelFont.dispose();
    }
}