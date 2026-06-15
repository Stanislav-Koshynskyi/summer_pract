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
import org.core.enums.MenuStatus;

public class DefeatMenu implements Screen {

    private static final Color BG_COLOR = new Color(0x0a0a0aff);
    private static final Color SOULS_RED = new Color(0x8b0000ff);
    private static final Color BTN_HOVER = new Color(0x4a5068ff);

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final int RADIUS = 12;

    private final com.badlogic.gdx.Game game;
    private final SwitchMenu switchMenu;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final GlyphLayout layout;

    private BitmapFont youDiedFont;
    private BitmapFont buttonFont;

    private final float btnW = 250f;
    private final float btnH = 65f;
    private final float btnGap = 30f;

    public DefeatMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;

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

            param.size = 120;
            param.color = SOULS_RED;
            youDiedFont = generator.generateFont(param);
            youDiedFont.setColor(SOULS_RED);

            param.size = 28;
            param.color = Color.LIGHT_GRAY;
            buttonFont = generator.generateFont(param);

            generator.dispose();
        } else {
            youDiedFont = new BitmapFont();
            youDiedFont.getData().setScale(5f);
            youDiedFont.setColor(SOULS_RED);
            buttonFont = new BitmapFont();
            buttonFont.setColor(Color.LIGHT_GRAY);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        if (game.getScreen() != this) return;

        camera.update();

        drawText();
        drawButtons();
        handleInput();
    }

    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;
        String diedText = isUa ? "ВИ ПОМЕРЛИ" : "YOU DIED";
        layout.setText(youDiedFont, diedText);
        youDiedFont.draw(batch, diedText, (WORLD_W - layout.width) / 2f, WORLD_H / 2f + 50f);

        batch.end();
    }

    private void drawButtons() {
        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;

        String retryText = isUa ? "ЩЕ РАЗ" : "RETRY";
        String menuText = isUa ? "МЕНЮ" : "MENU";

        float totalBtnsW = btnW * 2 + btnGap;
        float startX = (WORLD_W - totalBtnsW) / 2f;
        float btnY = WORLD_H / 2f - 120f;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        boolean retryHover = isMouseOver(startX, btnY, btnW, btnH);
        drawRoundRect(startX, btnY, btnW, btnH, RADIUS, retryHover ? BTN_HOVER : SOULS_RED);

        float menuBtnX = startX + btnW + btnGap;
        boolean menuHover = isMouseOver(menuBtnX, btnY, btnW, btnH);
        drawRoundRect(menuBtnX, btnY, btnW, btnH, RADIUS, menuHover ? BTN_HOVER : SOULS_RED);

        shapes.end();

        batch.begin();
        layout.setText(buttonFont, retryText);
        buttonFont.draw(batch, retryText, startX + (btnW - layout.width) / 2f, btnY + (btnH + layout.height) / 2f);

        layout.setText(buttonFont, menuText);
        buttonFont.draw(batch, menuText, menuBtnX + (btnW - layout.width) / 2f, btnY + (btnH + layout.height) / 2f);
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

        float totalBtnsW = btnW * 2 + btnGap;
        float startX = (WORLD_W - totalBtnsW) / 2f;
        float btnY = WORLD_H / 2f - 120f;
        if (wx >= startX && wx <= startX + btnW && wy >= btnY && wy <= btnY + btnH) {
            switchMenu.switchMenu(MenuStatus.PLAY_GAME_MENU);
        }
        float menuBtnX = startX + btnW + btnGap;
        if (wx >= menuBtnX && wx <= menuBtnX + btnW && wy >= btnY && wy <= btnY + btnH) {
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

    @Override
    public void show() {
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
        if (youDiedFont != null) youDiedFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
    }
}