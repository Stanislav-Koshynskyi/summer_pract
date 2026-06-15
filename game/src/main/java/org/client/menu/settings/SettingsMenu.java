package org.client.menu.settings;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.client.MainGame;
import org.client.LanguageUI;
import org.client.menu.SwitchMenu;


import java.util.ArrayList;
import java.util.List;

public class SettingsMenu implements Screen {

    private static final Color BG_COLOR      = new Color(0x1a1c26ff);
    private static final Color SIDEBAR_COLOR = new Color(0x11121aff);
    private static final Color ACCENT_GOLD   = new Color(0xffd700ff);
    private static final Color BTN_HOVER     = new Color(0x4a5068ff);
    private static final Color BORDER_COLOR  = new Color(0x2d3142ff);

    private static final float WORLD_W = 1280;
    private static final float WORLD_H = 720;
    private static final int RADIUS = 10;

    private final MainGame mainGame;
    private final SwitchMenu switchMenu;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GlyphLayout layout;

    public BitmapFont fontLarge;
    public BitmapFont fontMedium;
    public BitmapFont fontSmall;

    private final Vector3 mousePos = new Vector3();
    private final List<SettingsCategory> categories = new ArrayList<>();
    private int activeCategoryIndex = 0;
    private int activePageIndex = 0;

    private final float sidebarWidth = 260;
    private final float topBarHeight = 70;
    private final float backBtnW = 140;
    private final float backBtnH = 50;

    public SettingsMenu(Game game, SwitchMenu switchMenu) {
        this.mainGame = (MainGame) game;
        this.switchMenu = switchMenu;
        this.batch = new SpriteBatch();
        this.shapes = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        this.layout = new GlyphLayout();

        initFonts();
        initCategories();

        ((MainGame) game).playMenuMusic();
    }

    private void initCategories() {
        // --- 1. КЕРУВАННЯ (Нова вкладка) ---
        SettingsCategory controlsCategory = new SettingsCategory("КЕРУВАННЯ", "CONTROLS");
        controlsCategory.addPage(new KeyboardPage("Клавіатура", "Keyboard"));
        categories.add(controlsCategory);

        // --- 2. ЗВУК ---
        SettingsCategory soundCategory = new SettingsCategory("ЗВУК", "SOUND");
        StandardPage musicPage = new StandardPage("Музика", "Music");
        musicPage.addItem(new SliderItem("Рівень гучності музики", "Music Volume Level",
                "Фонова музика в меню та під час гри.", "Background music in menus and gameplay.", 0.7f));
        soundCategory.addPage(musicPage);

        StandardPage sfxPage = new StandardPage("Звукові ефекти", "Sound Effects");
        sfxPage.addItem(new SliderItem("Гучність ефектів", "Effects Volume",
                "Звуки кроків, кліків та взаємодій.", "Sounds of footsteps, clicks, and interactions.", 0.8f));
        soundCategory.addPage(sfxPage);
        categories.add(soundCategory);

        // --- 3. МОВА ---
        SettingsCategory langCategory = new SettingsCategory("МОВА", "LANGUAGE");
        StandardPage langPage = new StandardPage("Мова інтерфейсу", "Interface Language");
        langPage.addItem(new LanguageSelectorItem(mainGame, "Вибір мови", "Language Selection",
                "Оберіть мову для інтерфейсу гри.", "Choose the language for the game interface."));
        langCategory.addPage(langPage);
        categories.add(langCategory);
    }

    private void initFonts() {
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            parameter.size = 26; fontLarge = generator.generateFont(parameter);
            parameter.size = 22; fontMedium = generator.generateFont(parameter);
            parameter.size = 18; fontSmall = generator.generateFont(parameter);
            generator.dispose();
        } else {
            fontLarge = new BitmapFont(); fontMedium = new BitmapFont(); fontSmall = new BitmapFont();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);
        if (mainGame.getScreen() != this) return;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        boolean isUa = mainGame.getCurrentLanguage() == LanguageUI.UKRAINIAN;

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        float mx = mousePos.x; float my = mousePos.y;
        boolean justTouched = Gdx.input.justTouched();
        boolean isTouched = Gdx.input.isTouched();

        handleGlobalInput(mx, my, justTouched);
        if (mainGame.getScreen() != this) return;

        drawUI(mx, my, isUa);

        if (!categories.isEmpty()) {
            SettingsCategory activeCat = categories.get(activeCategoryIndex);
            if (!activeCat.pages.isEmpty()) {
                SettingsPage activePage = activeCat.pages.get(activePageIndex);
                activePage.handleInput(mx, my, justTouched, isTouched);
                if (mainGame.getScreen() != this) return;
                activePage.render(batch, shapes, fontLarge, fontMedium, fontSmall, sidebarWidth, topBarHeight, isUa);
            }
        }
    }

    private void handleGlobalInput(float mx, float my, boolean justTouched) {
        if (!justTouched) return;

        if (mx > 20 && mx < 20 + backBtnW && my > 20 && my < 20 + backBtnH) {
            switchMenu.popScreen(); return;
        }

        float catX = 20, catW = sidebarWidth - 40, catH = 50;
        float catStartY = WORLD_H - 100, catGap = 15;

        for (int i = 0; i < categories.size(); i++) {
            float y = catStartY - i * (catH + catGap);
            if (mx >= catX && mx <= catX + catW && my >= y && my <= y + catH) {
                if (activeCategoryIndex != i) {
                    activeCategoryIndex = i;
                    activePageIndex = 0;
                }
                return;
            }
        }

        if (!categories.isEmpty()) {
            SettingsCategory activeCat = categories.get(activeCategoryIndex);
            float tabStartX = sidebarWidth + 30, tabEndX = WORLD_W - 30;
            float tabW = activeCat.pages.isEmpty() ? 0 : (tabEndX - tabStartX - (activeCat.pages.size() - 1) * 15) / activeCat.pages.size();
            float tabY = WORLD_H - topBarHeight + (topBarHeight - 45) / 2f;

            float currentTabX = tabStartX;
            for (int i = 0; i < activeCat.pages.size(); i++) {
                if (mx >= currentTabX && mx <= currentTabX + tabW && my >= tabY && my <= tabY + 45) {
                    activePageIndex = i; return;
                }
                currentTabX += tabW + 15;
            }
        }
    }

    private void drawUI(float mx, float my, boolean isUa) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(SIDEBAR_COLOR);
        shapes.rect(0, 0, sidebarWidth, WORLD_H);
        shapes.rect(sidebarWidth, WORLD_H - topBarHeight, WORLD_W - sidebarWidth, topBarHeight);

        boolean backHovered = (mx > 20 && mx < 20 + backBtnW && my > 20 && my < 20 + backBtnH);
        drawRoundRectStatic(shapes, 20, 20, backBtnW, backBtnH, RADIUS, backHovered ? BTN_HOVER : Color.FIREBRICK);

        float catX = 20, catW = sidebarWidth - 40, catH = 50;
        float catStartY = WORLD_H - 100, catGap = 15;
        for (int i = 0; i < categories.size(); i++) {
            boolean isActive = (i == activeCategoryIndex);
            float y = catStartY - i * (catH + catGap);
            boolean isHovered = (mx >= catX && mx <= catX + catW && my >= y && my <= y + catH);
            drawRoundRectStatic(shapes, catX, y, catW, catH, RADIUS, isActive ? Color.DARK_GRAY : (isHovered ? BTN_HOVER : Color.BLACK));
        }

        SettingsCategory activeCat = categories.get(activeCategoryIndex);
        float tabStartX = sidebarWidth + 30, tabEndX = WORLD_W - 30;
        float gap = 15, tabH = 45;
        float tabW = activeCat.pages.isEmpty() ? 0 : (tabEndX - tabStartX - (activeCat.pages.size() - 1) * gap) / activeCat.pages.size();
        float tabY = WORLD_H - topBarHeight + (topBarHeight - tabH) / 2f;

        float currentTabX = tabStartX;
        for (int i = 0; i < activeCat.pages.size(); i++) {
            boolean isActive = (i == activePageIndex);
            boolean isHovered = (mx >= currentTabX && mx <= currentTabX + tabW && my >= tabY && my <= tabY + tabH);
            drawRoundRectStatic(shapes, currentTabX, tabY, tabW, tabH, RADIUS, isActive ? Color.DARK_GRAY : (isHovered ? BTN_HOVER : Color.BLACK));
            currentTabX += tabW + gap;
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BORDER_COLOR);
        shapes.rect(sidebarWidth, 0, 1, WORLD_H);
        shapes.rect(sidebarWidth, WORLD_H - topBarHeight, WORLD_W - sidebarWidth, 1);
        shapes.end();

        batch.begin();
        fontMedium.setColor(Color.WHITE);
        String backTxt = isUa ? "Назад" : "Back";
        layout.setText(fontMedium, backTxt);
        fontMedium.draw(batch, backTxt, 20 + (backBtnW - layout.width) / 2f, 20 + (backBtnH + layout.height) / 2f);

        for (int i = 0; i < categories.size(); i++) {
            boolean isActive = (i == activeCategoryIndex);
            float y = catStartY - i * (catH + catGap);
            boolean isHovered = (mx >= catX && mx <= catX + catW && my >= y && my <= y + catH);

            fontMedium.setColor(isActive ? ACCENT_GOLD : (isHovered ? Color.WHITE : Color.LIGHT_GRAY));
            String catName = categories.get(i).getName(isUa);
            layout.setText(fontMedium, catName);
            fontMedium.draw(batch, catName, catX + (catW - layout.width) / 2f, y + (catH + layout.height) / 2f);
        }

        currentTabX = tabStartX;
        for (int i = 0; i < activeCat.pages.size(); i++) {
            boolean isActive = (i == activePageIndex);
            boolean isHovered = (mx >= currentTabX && mx <= currentTabX + tabW && my >= tabY && my <= tabY + tabH);

            fontSmall.setColor(isActive ? ACCENT_GOLD : (isHovered ? Color.WHITE : Color.GRAY));
            String tabName = activeCat.pages.get(i).getName(isUa);

            float targetTextWidth = tabW - 20;
            layout.setText(fontSmall, tabName, fontSmall.getColor(), targetTextWidth, Align.center, true);
            fontSmall.draw(batch, tabName, currentTabX + 10, tabY + (tabH + layout.height) / 2f, targetTextWidth, Align.center, true);
            currentTabX += tabW + gap;
        }
        batch.end();
    }

    // Зроблено публічним статичним для використання в інших сторінках (напр. LanguageSelectorItem, KeyboardPage)
    public static void drawRoundRectStatic(ShapeRenderer shapes, float x, float y, float w, float h, int r, Color color) {
        shapes.setColor(color);
        shapes.rect(x + r, y, w - 2 * r, h);
        shapes.rect(x, y + r, r, h - 2 * r);
        shapes.rect(x + w - r, y + r, r, h - 2 * r);
        shapes.circle(x + r, y + r, r, 20);
        shapes.circle(x + w - r, y + r, r, 20);
        shapes.circle(x + r, y + h - r, r, 20);
        shapes.circle(x + w - r, y + h - r, r, 20);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        batch.dispose(); shapes.dispose();
        if (fontLarge != null) fontLarge.dispose();
        if (fontMedium != null) fontMedium.dispose();
        if (fontSmall != null) fontSmall.dispose();
    }
}