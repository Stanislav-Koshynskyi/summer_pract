package org.client.menu;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.client.LanguageUI;
import org.client.MainGame;
import org.client.MenuStatus;
import org.core.definition.PlayerProfile;


import java.util.List;

public class PlayerSelectScreen implements Screen {

    private static final Color BG_COLOR   = new Color(0x12121aff);
    private static final Color CARD_COLOR = new Color(0x1e1e2eff);
    private static final Color CARD_HOVER = new Color(0x32324aff);
    private static final Color GOLD       = new Color(0xf5c842ff);
    private static final Color WHITE      = new Color(0xeeeeffff);
    private static final Color DIM        = new Color(0xaaaaaaff);

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final int   RADIUS  = 12;

    private static final float CARD_W   = 300f;
    private static final float CARD_H   = 220f;
    private static final float CARD_GAP = 25f;
    private static final float SIDE_PADDING = 50f;

    private static final float BACK_BTN_W = 220f;
    private static final float BACK_BTN_H = 60f;
    private static final float BACK_BTN_X = (WORLD_W - BACK_BTN_W) / 2f;
    private static final float BACK_BTN_Y = 50f;

    private final com.badlogic.gdx.Game game;
    private final SwitchMenu switchMenu;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final GlyphLayout layout;

    private BitmapFont titleFont;
    private BitmapFont nameFont;
    private BitmapFont descFont;

    private final List<PlayerProfile> profiles;
    private final float[] cardX;
    private final float cardY;
    private float scrollX = 0f;
    private float maxScrollX;

    public PlayerSelectScreen(com.badlogic.gdx.Game game, SwitchMenu switchMenu, List<PlayerProfile> profiles) {
        this.game = game;
        this.switchMenu = switchMenu;
        this.profiles = profiles;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        shapes.setAutoShapeType(true);
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        initFonts();

        int count = profiles.size();
        float totalCardsWidth = count * CARD_W + (count - 1) * CARD_GAP;
        float startX = SIDE_PADDING;
        maxScrollX = Math.max(0, totalCardsWidth - (WORLD_W - 2 * SIDE_PADDING));

        cardX = new float[count];
        for (int i = 0; i < count; i++) {
            cardX[i] = startX + i * (CARD_W + CARD_GAP);
        }
        cardY = (WORLD_H - CARD_H) / 2f + 20f;
    }

    private void initFonts() {
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            param.size = 44;
            titleFont = generator.generateFont(param);
            titleFont.setColor(GOLD);

            param.size = 26;
            nameFont = generator.generateFont(param);
            nameFont.setColor(WHITE);

            param.size = 20;
            descFont = generator.generateFont(param);
            descFont.setColor(DIM);

            generator.dispose();
        } else {
            titleFont = new BitmapFont(); nameFont = new BitmapFont(); descFont = new BitmapFont();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollX -= amountY * 30;
                if (scrollX < 0) scrollX = 0;
                if (scrollX > maxScrollX) scrollX = maxScrollX;
                return true;
            }
        });
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

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < profiles.size(); i++) {
            float x = cardX[i] - scrollX;
            boolean hovered = isMouseOver(x, cardY, CARD_W, CARD_H);
            drawRoundRect(x, cardY, CARD_W, CARD_H, RADIUS, hovered ? CARD_HOVER : CARD_COLOR);
        }

        boolean backHovered = isMouseOver(BACK_BTN_X, BACK_BTN_Y, BACK_BTN_W, BACK_BTN_H);
        drawRoundRect(BACK_BTN_X, BACK_BTN_Y, BACK_BTN_W, BACK_BTN_H, RADIUS, backHovered ? CARD_HOVER : CARD_COLOR);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        shapes.setColor(GOLD);
        for (int i = 0; i < profiles.size(); i++) {
            float x = cardX[i] - scrollX;
            drawRoundRectOutline(x, cardY, CARD_W, CARD_H, RADIUS);
        }
        shapes.setColor(DIM);
        drawRoundRectOutline(BACK_BTN_X, BACK_BTN_Y, BACK_BTN_W, BACK_BTN_H, RADIUS);
        shapes.end();
        Gdx.gl.glLineWidth(1f);
    }

    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;

        String title = isUa ? "ОБЕРИ БІЙЦЯ" : "SELECT FIGHTER";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, (WORLD_W - layout.width) / 2f, WORLD_H - 40f);

        for (int i = 0; i < profiles.size(); i++) {
            PlayerProfile p = profiles.get(i);
            float x = cardX[i] - scrollX;
            float cx = x + CARD_W / 2f;
            float y = cardY + CARD_H - 25f;

            String name = p.getCurrentName(isUa);
            layout.setText(nameFont, name);
            nameFont.draw(batch, name, cx - layout.width / 2f, y);

            y -= 35f;
            String desc = p.getCurrentDescription(isUa);
            layout.setText(descFont, desc, Color.WHITE, CARD_W - 30, Align.center, true);
            descFont.draw(batch, desc, x + 15, y, CARD_W - 30, Align.center, true);
        }

        String back = isUa ? "← НАЗАД" : "← BACK";
        descFont.setColor(DIM);
        layout.setText(descFont, back);
        descFont.draw(batch, back, BACK_BTN_X + (BACK_BTN_W - layout.width) / 2f, BACK_BTN_Y + (BACK_BTN_H + layout.height) / 2f);

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

        if (wx >= BACK_BTN_X && wx <= BACK_BTN_X + BACK_BTN_W && wy >= BACK_BTN_Y && wy <= BACK_BTN_Y + BACK_BTN_H) {
            switchMenu.switchMenu(MenuStatus.START_MENU);
            return;
        }

        for (int i = 0; i < profiles.size(); i++) {
            float x = cardX[i] - scrollX;
            if (wx >= x && wx <= x + CARD_W && wy >= cardY && wy <= cardY + CARD_H) {
                ((MainGame) game).setCurrentPlayerId(profiles.get(i).getId());
                switchMenu.switchMenu(MenuStatus.SELECT_LEVEL_MENU);
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

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        shapes.dispose();
        batch.dispose();
        titleFont.dispose();
        nameFont.dispose();
        descFont.dispose();
    }
}