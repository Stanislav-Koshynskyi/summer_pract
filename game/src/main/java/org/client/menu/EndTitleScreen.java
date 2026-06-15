package org.client.menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import org.client.MainGame;
import org.client.LanguageUI;
import org.client.MenuStatus;

public class EndTitleScreen implements Screen {

    private static final Color BG_COLOR      = Color.valueOf("12121aff");
    private static final Color PANEL_BG      = Color.valueOf("1e1e2eff");
    private static final Color PANEL_BORDER  = Color.valueOf("3a3a5cff");
    private static final Color GOLD          = Color.valueOf("f5c842ff");
    private static final Color WHITE         = Color.valueOf("eeeef5ff");
    private static final Color DIM           = Color.valueOf("aaaaaaff");
    private static final Color GREEN         = Color.valueOf("4ecb71ff");
    private static final Color RED           = Color.valueOf("e05252ff");

    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;
    private static final int RADIUS = 12;

    private final Game game;
    private final SwitchMenu switchMenu;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private BitmapFont titleFont;
    private BitmapFont nameFont;
    private BitmapFont textFont;

    private float scrollOffsetY = 0f;
    private float maxScrollY = 0f;
    private float totalContentHeight = 0f;
    private float autoScrollSpeed = 30f; // pixels per second
    private boolean userInteracted = false;
    private float autoScrollResumeTimer = 0f;

    // Content bounds
    private final float contentLeft = 190f;
    private final float contentWidth = 900f;
    private final float contentTop = WORLD_H - 160f;
    private final float contentBottom = 60f;
    private final float contentHeight = contentTop - contentBottom;

    // Back Button Geometry
    private final float backBtnX = 50f;
    private final float backBtnY = WORLD_H - 120f;
    private final float backBtnW = 140f;
    private final float backBtnH = 50f;

    private static class CreditItem {
        String name;
        String role;
        String[] details;

        CreditItem(String name, String role, String[] details) {
            this.name = name;
            this.role = role;
            this.details = details;
        }
    }

    private CreditItem[] creditsUa;
    private CreditItem[] creditsEn;

    public EndTitleScreen(Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;
        this.batch = new SpriteBatch();
        this.shapes = new ShapeRenderer();
        this.shapes.setAutoShapeType(true);
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_W, WORLD_H, camera);

        initFonts();
        initCreditsData();
        calculateMaxScroll();
    }

    private void initFonts() {
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            param.size = 36;
            titleFont = generator.generateFont(param);
            titleFont.setColor(GOLD);

            param.size = 24;
            nameFont = generator.generateFont(param);
            nameFont.setColor(GOLD);

            param.size = 18;
            textFont = generator.generateFont(param);
            textFont.setColor(WHITE);

            generator.dispose();
        } else {
            titleFont = new BitmapFont();
            nameFont = new BitmapFont();
            textFont = new BitmapFont();
        }
    }

    private void initCreditsData() {
        creditsUa = new CreditItem[] {
            new CreditItem(
                "Станіслав Кошинський",
                "Розробник А (Core)",
                new String[] {
                    "- Створення об'єктної моделі гри (гравець, вороги, стани)",
                    "- WorldGeometry, CollisionSystem та обробка колізій стін",
                    "- Розробка штучного інтелекту EnemyAI та VisionSystem",
                    "- Алгоритм пошуку шляху A* (Pathfinding) та steering",
                    "- Система поведінки зброї (WeaponBehavior)"
                }
            ),
            new CreditItem(
                "Роман Гвоздік",
                "Розробник Б (Client)",
                new String[] {
                    "- Налаштування LibGDX, камери та Viewport",
                    "- Парсинг об'єктів Tiled-карт (LevelTmxLoader)",
                    "- Анімації персонажів та відкриття дверей",
                    "- Фонова музика та звукове оформлення гри",
                    "- Дебаг-панель (F1) та екрани гри"
                }
            ),
            new CreditItem(
                "Нікіта Різник",
                "Розробник В (Content + Core systems)",
                new String[] {
                    "- InteractionSystem: відкриття дверей, безшумні вбивства",
                    "- HearingSystem та SoundEventQueue (слух ворогів)",
                    "- FootstepEmitter (генерація кроків відповідно до руху)",
                    "- Збереження та завантаження прогресу в JSON (SaveManager)",
                    "- Кампанія, меню вибору рівнів та статистика проходження"
                }
            ),
            new CreditItem(
                "Юрій Волошанович",
                "Розробник Г (Level Design)",
                new String[] {
                    "- Створення ігрових рівнів та тестових кімнат у Tiled",
                    "- Розробка ігрових ситуацій (assault/stealth)",
                    "- Балансування складності та поведінки ворогів"
                }
            ),
            new CreditItem(
                "Богдан Кравчук",
                "Дизайнер (Art / Audio / Media)",
                new String[] {
                    "- Дизайн інтерфейсу меню та мокапи екранів",
                    "- Підбір та створення графічних ассетів і спрайтів",
                    "- Робота над звуковими ефектами та візуальним стилем"
                }
            )
        };

        creditsEn = new CreditItem[] {
            new CreditItem(
                "Stanislav Koshynskyi",
                "Developer A (Core)",
                new String[] {
                    "- Game entity model creation (player, enemy, levels)",
                    "- WorldGeometry, CollisionSystem and collision handling",
                    "- EnemyAI development and VisionSystem (field of view)",
                    "- A* pathfinding algorithm implementation and steering",
                    "- WeaponBehavior system abstraction"
                }
            ),
            new CreditItem(
                "Roman Hvozdik",
                "Developer B (Client)",
                new String[] {
                    "- LibGDX framework setup, camera and viewports",
                    "- Tiled map object parsing (LevelTmxLoader)",
                    "- Door opening and character animations",
                    "- Background music and sound mapping",
                    "- Debug overlay (F1) and screen layouts"
                }
            ),
            new CreditItem(
                "Nikita Riznyk",
                "Developer C (Content + Core systems)",
                new String[] {
                    "- InteractionSystem: opening doors, silent kills",
                    "- HearingSystem and SoundEventQueue (hearing mechanics)",
                    "- FootstepEmitter (noise emission timing based on movement)",
                    "- JSON progress save/load management (SaveManager)",
                    "- Campaign select menu, statistics and progression"
                }
            ),
            new CreditItem(
                "Yurii Voloshanovych",
                "Developer D (Level Design)",
                new String[] {
                    "- Level design and test maps creation via Tiled Map Editor",
                    "- Sandbox and stealth/assault scenario planning",
                    "- Enemy behavior and stats balancing"
                }
            ),
            new CreditItem(
                "Bohdan Kravchuk",
                "Designer (Art / Audio / Media)",
                new String[] {
                    "- Game menu user interface design & screen mockups",
                    "- Asset sourcing, sprite sheets and texture creation",
                    "- Audio/visual direction and styling"
                }
            )
        };
    }

    private void calculateMaxScroll() {
        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;
        CreditItem[] items = isUa ? creditsUa : creditsEn;

        float h = 0f;
        for (CreditItem item : items) {
            h += 30f; // Name space
            h += 25f; // Role space
            h += item.details.length * 22f; // Details space
            h += 50f; // Card padding & gap
        }
        h += 120f; // Additional space at bottom for the "MAIN MENU" button
        totalContentHeight = h;
        maxScrollY = Math.max(0f, totalContentHeight - contentHeight);
    }

    @Override
    public void show() {
        calculateMaxScroll();
        scrollOffsetY = 0f;
        userInteracted = false;
        autoScrollResumeTimer = 0f;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollOffsetY += amountY * 30f;
                clampScroll();
                userInteracted = true;
                autoScrollResumeTimer = 5f; // Pause auto-scrolling for 5 seconds
                return true;
            }
        });
    }

    private void clampScroll() {
        if (scrollOffsetY < 0f) scrollOffsetY = 0f;
        if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
    }

    private void handleInput(float wx, float wy) {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scrollOffsetY -= 150f * Gdx.graphics.getDeltaTime();
            clampScroll();
            userInteracted = true;
            autoScrollResumeTimer = 5f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scrollOffsetY += 150f * Gdx.graphics.getDeltaTime();
            clampScroll();
            userInteracted = true;
            autoScrollResumeTimer = 5f;
        }

        if (Gdx.input.justTouched()) {
            // Check back button
            if (wx >= backBtnX && wx <= backBtnX + backBtnW && wy >= backBtnY && wy <= backBtnY + backBtnH) {
                switchMenu.switchMenu(MenuStatus.START_MENU);
                return;
            }

            // Check main menu button at bottom of scrolling content
            boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;
            float btnW = 300f;
            float btnH = 60f;
            float btnX = (WORLD_W - btnW) / 2f;
            float btnYInList = -totalContentHeight + 80f; // relative Y from currentY start
            float btnWorldY = contentTop + scrollOffsetY + btnYInList;

            // Only check click if button is within visible scissor viewport
            if (btnWorldY >= contentBottom && btnWorldY <= contentTop) {
                if (wx >= btnX && wx <= btnX + btnW && wy >= btnWorldY && wy <= btnWorldY + btnH) {
                    switchMenu.switchMenu(MenuStatus.START_MENU);
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        if (game.getScreen() != this) return;

        // Auto-scroll logic
        if (userInteracted) {
            autoScrollResumeTimer -= delta;
            if (autoScrollResumeTimer <= 0f) {
                userInteracted = false;
            }
        } else {
            if (scrollOffsetY < maxScrollY) {
                scrollOffsetY += autoScrollSpeed * delta;
                clampScroll();
            }
        }

        com.badlogic.gdx.math.Vector3 touchPos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);
        float wx = touchPos.x;
        float wy = touchPos.y;

        handleInput(wx, wy);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        boolean isUa = ((MainGame) game).getCurrentLanguage() == LanguageUI.UKRAINIAN;

        // =========================================================================
        // Draw Static Header Shapes
        // =========================================================================
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        boolean backHovered = wx >= backBtnX && wx <= backBtnX + backBtnW && wy >= backBtnY && wy <= backBtnY + backBtnH;
        drawRoundRect(backBtnX, backBtnY, backBtnW, backBtnH, RADIUS, backHovered ? RED : PANEL_BG);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        shapes.setColor(backHovered ? GOLD : PANEL_BORDER);
        drawRoundRectOutline(backBtnX, backBtnY, backBtnW, backBtnH, RADIUS);
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // =========================================================================
        // Draw Static Header Text
        // =========================================================================
        batch.begin();
        // Back Button Text
        textFont.setColor(WHITE);
        String backText = isUa ? "НАЗАД" : "BACK";
        layout.setText(textFont, backText);
        textFont.draw(batch, backText, backBtnX + (backBtnW - layout.width) / 2f, backBtnY + (backBtnH + layout.height) / 2f);

        // Screen Title
        String mainTitle = isUa ? "ДЯКУЄМО ЗА ГРУ!" : "THANK YOU FOR PLAYING!";
        titleFont.setColor(GOLD);
        layout.setText(titleFont, mainTitle);
        titleFont.draw(batch, mainTitle, (WORLD_W - layout.width) / 2f, WORLD_H - 40f);

        String subtitle = isUa ? "АВТОРИ ПРОЄКТУ" : "PROJECT CREDITS";
        textFont.setColor(DIM);
        layout.setText(textFont, subtitle);
        textFont.draw(batch, subtitle, (WORLD_W - layout.width) / 2f, WORLD_H - 95f);
        batch.end();

        // =========================================================================
        // Draw Scissored Scroll Content (Author Cards)
        // =========================================================================
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        float screenScaleX = (float) viewport.getScreenWidth() / WORLD_W;
        float screenScaleY = (float) viewport.getScreenHeight() / WORLD_H;

        int scissorX = Math.round(viewport.getScreenX() + contentLeft * screenScaleX);
        int scissorY = Math.round(viewport.getScreenY() + contentBottom * screenScaleY);
        int scissorW = Math.round(contentWidth * screenScaleX);
        int scissorH = Math.round(contentHeight * screenScaleY);
        Gdx.gl.glScissor(scissorX, scissorY, scissorW, scissorH);

        CreditItem[] items = isUa ? creditsUa : creditsEn;
        float currentY = contentTop + scrollOffsetY;

        // Draw Card Backgrounds first
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float tempY = currentY;
        for (CreditItem item : items) {
            float cardH = 30f + 25f + item.details.length * 22f + 40f;
            drawRoundRect(contentLeft, tempY - cardH, contentWidth, cardH, RADIUS, PANEL_BG);
            tempY -= cardH + 20f;
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        tempY = currentY;
        for (CreditItem item : items) {
            float cardH = 30f + 25f + item.details.length * 22f + 40f;
            shapes.setColor(PANEL_BORDER);
            drawRoundRectOutline(contentLeft, tempY - cardH, contentWidth, cardH, RADIUS);
            tempY -= cardH + 20f;
        }
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        // Draw Card Texts
        batch.begin();
        tempY = currentY;
        for (CreditItem item : items) {
            float cardH = 30f + 25f + item.details.length * 22f + 40f;
            float textY = tempY - 25f;

            // Name
            nameFont.setColor(GOLD);
            nameFont.draw(batch, item.name, contentLeft + 30f, textY);
            textY -= 32f;

            // Role
            textFont.setColor(GREEN);
            textFont.draw(batch, item.role, contentLeft + 30f, textY);
            textY -= 25f;

            // Details
            textFont.setColor(WHITE);
            for (String detail : item.details) {
                textFont.draw(batch, detail, contentLeft + 45f, textY);
                textY -= 22f;
            }

            tempY -= cardH + 20f;
        }

        // Draw Main Menu Button at the bottom of the credits list
        batch.end();

        // Menu button backgrounds
        float btnW = 300f;
        float btnH = 60f;
        float btnX = (WORLD_W - btnW) / 2f;
        float btnYInList = -totalContentHeight + 80f;
        float btnWorldY = contentTop + scrollOffsetY + btnYInList;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        boolean btnHovered = wx >= btnX && wx <= btnX + btnW && wy >= btnWorldY && wy <= btnWorldY + btnH;
        drawRoundRect(btnX, btnWorldY, btnW, btnH, RADIUS, btnHovered ? GOLD : RED);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        shapes.setColor(btnHovered ? WHITE : PANEL_BORDER);
        drawRoundRectOutline(btnX, btnWorldY, btnW, btnH, RADIUS);
        shapes.end();
        Gdx.gl.glLineWidth(1f);

        batch.begin();
        textFont.setColor(btnHovered ? Color.BLACK : WHITE);
        String menuBtnText = isUa ? "ГОЛОВНЕ МЕНЮ" : "MAIN MENU";
        layout.setText(textFont, menuBtnText);
        textFont.draw(batch, menuBtnText, btnX + (btnW - layout.width) / 2f, btnWorldY + (btnH + layout.height) / 2f);
        batch.end();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        // Draw scroll bar if content exceeds viewport
        if (maxScrollY > 0f) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(PANEL_BG);
            shapes.rect(WORLD_W - 40f, contentBottom, 8f, contentHeight);

            float scrollThumbH = 60f;
            float scrollThumbY = contentTop - scrollThumbH - ((scrollOffsetY / maxScrollY) * (contentHeight - scrollThumbH));
            drawRoundRect(WORLD_W - 40f, scrollThumbY, 8f, scrollThumbH, 4, GOLD);
            shapes.end();
        }
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
        batch.dispose();
        shapes.dispose();
        titleFont.dispose();
        nameFont.dispose();
        textFont.dispose();
    }
}
