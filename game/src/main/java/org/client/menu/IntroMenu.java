package org.client.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import org.client.MainGame;
import org.client.LanguageUI;
import org.client.MenuStatus;

import java.io.FileNotFoundException;

public class IntroMenu implements Screen {

    private static final Color BG_COLOR      = new Color(0x1a1c24ff);
    private static final Color BTN_COLOR     = new Color(0x3a3f52ff);
    private static final Color BTN_BORDER    = new Color(0x6b7280ff);
    private static final Color BTN_HOVER     = new Color(0x4a5068ff);

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    // Розміри відео (90% від екрану)
    private static final float VIDEO_W = WORLD_W * 0.9f;
    private static final float VIDEO_H = WORLD_H * 0.75f;

    // Розміри кнопки (40% ширини екрану)
    private static final float BTN_W = WORLD_W * 0.4f;
    private static final float BTN_H = 70f;
    private static final int RADIUS = 18;
    private static final float BORDER = 3f;

    private final com.badlogic.gdx.Game game;
    private final SwitchMenu switchMenu;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private final String skipText;
    private final float btnX, btnY;
    private final float videoX, videoY;

    // Використовуємо VideoPlayer напряму
    private VideoPlayer videoPlayer;
    private boolean isVideoLoaded = false;
    private boolean isExiting = false; // Захист від використання знищених ресурсів

    public IntroMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;

        MainGame mainGame = (MainGame) game;
        if (mainGame.getCurrentLanguage() == LanguageUI.ENGLISH) {
            skipText = "Skip";
        } else {
            skipText = "Пропустити";
        }

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        layout = new GlyphLayout();

        // Завантаження шрифту
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";
            parameter.size = 28;
            font = generator.generateFont(parameter);
            generator.dispose();
        } else {
            font = new BitmapFont();
        }

        // Розрахунок позицій (Центрування)
        videoX = (WORLD_W - VIDEO_W) / 2f;
        videoY = WORLD_H * 0.2f;

        btnX = (WORLD_W - BTN_W) / 2f;
        btnY = videoY - BTN_H - 20f;

        // Ініціалізуємо та запускаємо відео ОДИН раз при створенні екрану
        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
            videoPlayer.load(Gdx.files.internal("video/chat.webm"));
            videoPlayer.play();
            isVideoLoaded = true;
        } catch (FileNotFoundException e) {
            Gdx.app.error("Video", "Файл відео movies/cutscene.mp4 не знайдено!", e);
        } catch (Exception e) {
            Gdx.app.error("Video", "Помилка створення або відтворення плеєра", e);
        }
    }

    @Override
    public void render(float delta) {
        // Якщо процес виходу вже запущено, негайно припиняємо будь-яке малювання
        if (isExiting) return;

        ScreenUtils.clear(BG_COLOR);
        camera.update();

        // 1. Оновлення кадрів відео та його малювання
        if (isVideoLoaded) {
            videoPlayer.update(); // Декодування наступного кадру

            Texture frame = videoPlayer.getTexture();
            if (frame != null) {
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                batch.draw(frame, videoX, videoY, VIDEO_W, VIDEO_H);
                batch.end();
            }

            // Автоматичний перехід далі, якщо відео закінчилося
            if (!videoPlayer.isPlaying()) {
                exitToMenu();
                return; // КРИТИЧНО: негайно виходимо з методу, щоб код нижче не виконувався!
            }
        }

        // 2. Малювання рамки навколо відео
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(videoX, videoY, VIDEO_W, VIDEO_H);
        shapes.end();

        // 3. Малювання кнопки Пропустити
        drawButton();

        // 4. Текст на кнопці
        drawText();

        // 5. Логіка натискання
        handleInput();
    }

    private void drawButton() {
        boolean hovered = isMouseOver();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRoundRect(btnX, btnY, BTN_W, BTN_H, RADIUS, hovered ? BTN_HOVER : BTN_COLOR);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BTN_BORDER);
        Gdx.gl.glLineWidth(BORDER);
        drawRoundRectOutline(btnX, btnY, BTN_W, BTN_H, RADIUS);
        shapes.end();
        Gdx.gl.glLineWidth(1f);
    }

    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        layout.setText(font, skipText);
        font.draw(batch, skipText, btnX + (BTN_W - layout.width) / 2f, btnY + (BTN_H + layout.height) / 2f);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (isMouseOver()) {
                exitToMenu();
            }
        }
    }

    private void exitToMenu() {
        if (isExiting) return; // Якщо вже виходимо, ігноруємо повторні виклики
        isExiting = true;
        // Зупиняємо плеєр перед виходом
        if (videoPlayer != null && videoPlayer.isPlaying()) {
            videoPlayer.stop();
        }
        // Переходимо на ігровий рівень (назва статусу з твого MainGame.java)
        switchMenu.switchMenu(MenuStatus.PLAY_GAME_MENU);
    }

    private boolean isMouseOver() {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();

        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;

        return wx >= btnX && wx <= btnX + BTN_W && wy >= btnY && wy <= btnY + BTN_H;
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

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        if (videoPlayer != null) {
            videoPlayer.dispose(); // Звільняємо пам'ять ОС від декодера відео
        }
    }
}