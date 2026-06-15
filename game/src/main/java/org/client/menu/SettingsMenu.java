package org.client.menu;

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

import java.util.ArrayList;
import java.util.List;

public class SettingsMenu implements Screen {

    // Стилізована колірна палітра
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

    private final Vector3 mousePos = new Vector3();

    public BitmapFont fontLarge;
    public BitmapFont fontMedium;
    public BitmapFont fontSmall;

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
    }

    // =========================================================================
    // АРХІТЕКТУРА OPEN/CLOSED PRINCIPLE (OCP)
    // =========================================================================

    public interface SettingsPage {
        String getName(boolean isUa);
        void handleInput(float mx, float my, boolean justTouched, boolean isTouched);
        void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float sidebarW, float topBarH, boolean isUa);
    }

    /**
     * Базовий клас для будь-якого елемента налаштувань.
     * Відкритий для розширення (можна легко додати Checkbox, Dropdown тощо),
     * закритий для модифікацій базового меню.
     */
    public abstract class SettingsItem {
        public abstract float getHeight(); // Скільки місця по вертикалі займає елемент
        public abstract void handleInput(float mx, float my, boolean jt, boolean it, float startX, float startY);
        public abstract void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float startX, float startY, boolean isUa);
    }

    /**
     * Стандартна сторінка, яка просто відображає додані до неї елементи налаштувань (SettingsItem)
     * зверху вниз.
     */
    public class StandardPage implements SettingsPage {
        private final String nameUa, nameEn;
        private final List<SettingsItem> items = new ArrayList<>();

        public StandardPage(String nameUa, String nameEn) {
            this.nameUa = nameUa;
            this.nameEn = nameEn;
        }

        public void addItem(SettingsItem item) { items.add(item); }

        @Override public String getName(boolean isUa) { return isUa ? nameUa : nameEn; }

        @Override
        public void handleInput(float mx, float my, boolean jt, boolean it) {
            float currentY = WORLD_H - 120; // Початкова позиція для першого елемента
            for (SettingsItem item : items) {
                item.handleInput(mx, my, jt, it, sidebarWidth + 50, currentY);
                currentY -= item.getHeight(); // Зсув вниз для наступного елемента
            }
        }

        @Override
        public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float sidebarW, float topBarH, boolean isUa) {
            float currentY = WORLD_H - 120;
            for (SettingsItem item : items) {
                item.render(batch, shapes, fontL, fontM, fontS, sidebarW + 50, currentY, isUa);
                currentY -= item.getHeight();
            }
        }
    }

    public class SettingsCategory {
        public String nameUa, nameEn;
        public List<SettingsPage> pages = new ArrayList<>();

        public SettingsCategory(String nameUa, String nameEn) {
            this.nameUa = nameUa;
            this.nameEn = nameEn;
        }

        public String getName(boolean isUa) { return isUa ? nameUa : nameEn; }
        public void addPage(SettingsPage page) { pages.add(page); }
    }

    // =========================================================================
    // КОНКРЕТНІ ЕЛЕМЕНТИ НАЛАШТУВАНЬ
    // =========================================================================

    public class SliderItem extends SettingsItem {
        private String titleUa, titleEn, descUa, descEn;
        private float value;
        private boolean isDragging = false;
        private final float sliderW = 350, sliderH = 15;
        private final SliderType type;

        public SliderItem(String tUa, String tEn, String dUa, String dEn, float defVal, SliderType type) {
            this.titleUa = tUa; this.titleEn = tEn;
            this.descUa = dUa; this.descEn = dEn;
            this.value = defVal;
            this.type = type;
        }

        @Override public float getHeight() { return 170; }

        @Override
        public void handleInput(float mx, float my, boolean jt, boolean it, float startX, float startY) {
            float sliderY = startY - 140;
            if (jt) {
                if (mx >= startX - 20 && mx <= startX + sliderW + 20 && my >= sliderY - 20 && my <= sliderY + sliderH + 20) {
                    isDragging = true;
                }
            }

            /// Change value slicer sound
            if (it && isDragging) {
                value = (mx - startX) / sliderW;
                if (value < 0) value = 0;
                if (value > 1) value = 1;

                if (type == SliderType.MUSIC) {
                    mainGame.setMusicVolume(value * 0.1f);
                    mainGame.updateMusicVolume();
                } else if (type == SliderType.SFX) {
                    mainGame.setSfxVolume(value * 0.01f);
                }
            } else if (!it) {
                isDragging = false;
            }
        }

        @Override
        public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float startX, float startY, boolean isUa) {
            // Розподіляємо елементи по вертикалі з безпечними проміжками
            float titleY = startY - 25;       // Заголовок (розмір 26) - трохи відступаємо від верху
            float descY = startY - 60;        // Опис (розмір 18) - 35 пікселів під заголовком
            float labelY = startY - 100;      // Текст "Гучність" (розмір 22) - 40 пікселів під описом
            float sliderY = startY - 140;     // Сам повзунок - ще нижче під текстом

            batch.begin();
            fontL.setColor(Color.WHITE);
            fontL.draw(batch, isUa ? titleUa : titleEn, startX, titleY);

            fontS.setColor(Color.GRAY);
            fontS.draw(batch, isUa ? descUa : descEn, startX, descY);

            fontM.setColor(ACCENT_GOLD);
            fontM.draw(batch, isUa ? "Гучність" : "Volume", startX, labelY);
            fontM.draw(batch, (int)(value * 100) + "%", startX + sliderW + 30, labelY);
            batch.end();

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(new Color(0x3a3f52ff));
            shapes.rect(startX, sliderY, sliderW, sliderH);

            shapes.setColor(ACCENT_GOLD);
            float filledWidth = sliderW * value;
            if (filledWidth > 0) shapes.rect(startX, sliderY, filledWidth, sliderH);

            shapes.circle(startX + filledWidth, sliderY + sliderH / 2, 14);
            shapes.end();
        }
    }

    public class LanguageSelectorItem extends SettingsItem {
        private String titleUa, titleEn, descUa, descEn;
        private float btnW = 150, btnH = 45;

        public LanguageSelectorItem(String tUa, String tEn, String dUa, String dEn) {
            this.titleUa = tUa; this.titleEn = tEn;
            this.descUa = dUa; this.descEn = dEn;
        }

        @Override public float getHeight() { return 150; }

        @Override
        public void handleInput(float mx, float my, boolean jt, boolean it, float startX, float startY) {
            if (!jt) return;
            float btnY = startY - 130;
            float enX = startX + btnW + 20;

            // Натискання на кнопку UA
            if (mx >= startX && mx <= startX + btnW && my >= btnY && my <= btnY + btnH) {
                mainGame.setCurrentLanguage(LanguageUI.UKRAINIAN);
            }
            // Натискання на кнопку EN
            if (mx >= enX && mx <= enX + btnW && my >= btnY && my <= btnY + btnH) {
                mainGame.setCurrentLanguage(LanguageUI.ENGLISH);
            }
        }

        @Override
        public void render(SpriteBatch batch, ShapeRenderer shapes, BitmapFont fontL, BitmapFont fontM, BitmapFont fontS, float startX, float startY, boolean isUa) {
            float titleY = startY - 25;
            float descY = startY - 60;
            float btnY = startY - 130; // Кнопки посунуті вниз, тепер опис їх не торкається
            float enX = startX + btnW + 20;

            batch.begin();
            fontL.setColor(Color.WHITE);
            fontL.draw(batch, isUa ? titleUa : titleEn, startX, titleY);

            fontS.setColor(Color.GRAY);
            fontS.draw(batch, isUa ? descUa : descEn, startX, descY);
            batch.end();

            boolean isCurrentUa = mainGame.getCurrentLanguage() == LanguageUI.UKRAINIAN;

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawRoundRect(startX, btnY, btnW, btnH, 8, isCurrentUa ? ACCENT_GOLD : new Color(0x3a3f52ff));
            drawRoundRect(enX, btnY, btnW, btnH, 8, !isCurrentUa ? ACCENT_GOLD : new Color(0x3a3f52ff));
            shapes.end();

            batch.begin();
            fontM.setColor(isCurrentUa ? Color.BLACK : Color.WHITE);
            layout.setText(fontM, "Українська");
            // Формула для ідеального центрування тексту по баз-лінії: Y_координата + (Висота_Кнопки - Висота_Тексту)/2 + Висота_Тексту
            fontM.draw(batch, "Українська", startX + (btnW - layout.width)/2f, btnY + (btnH - layout.height)/2f + layout.height);

            fontM.setColor(!isCurrentUa ? Color.BLACK : Color.WHITE);
            layout.setText(fontM, "English");
            fontM.draw(batch, "English", enX + (btnW - layout.width)/2f, btnY + (btnH - layout.height)/2f + layout.height);
            batch.end();
        }
    }

    // =========================================================================
    // ІНІЦІАЛІЗАЦІЯ МЕНЮ ЗГІДНО З ВИМОГАМИ
    // =========================================================================

    private void initCategories() {
        // --- 1. Категорія ЗВУК ---
        SettingsCategory soundCategory = new SettingsCategory("ЗВУК", "SOUND");

        // Сторінка "Музика"
        StandardPage musicPage = new StandardPage("Музика", "Music");
        musicPage.addItem(new SliderItem("Рівень гучності музики", "Music Volume Level",
                "Фонова музика в меню та під час гри.", "Background music in menus and gameplay.", mainGame.getMusicVolume() * 10, SliderType.MUSIC));
        soundCategory.addPage(musicPage);

        // Сторінка "Звукові ефекти"
        StandardPage sfxPage = new StandardPage("Звукові ефекти", "Sound Effects");
        sfxPage.addItem(new SliderItem("Гучність ефектів", "Effects Volume",
                "Звуки кроків, кліків та взаємодій.", "Sounds of footsteps, clicks, and interactions.", mainGame.getSfxVolume() * 100, SliderType.SFX));
        soundCategory.addPage(sfxPage);

        categories.add(soundCategory);

        // --- 2. Категорія МОВА ---
        SettingsCategory langCategory = new SettingsCategory("МОВА", "LANGUAGE");

        // Сторінка "Мова інтерфейсу"
        StandardPage langPage = new StandardPage("Мова інтерфейсу", "Interface Language");
        langPage.addItem(new LanguageSelectorItem("Вибір мови", "Language Selection",
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
    public void show() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR);

        // Базова перевірка (раптом нас вже змінили до render)
        if (mainGame.getScreen() != this) return;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        boolean isUa = mainGame.getCurrentLanguage() == LanguageUI.UKRAINIAN;

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        float mx = mousePos.x;
        float my = mousePos.y;

        boolean justTouched = Gdx.input.justTouched();
        boolean isTouched = Gdx.input.isTouched();

        // Обробка глобальних кліків (Назад, Сайдбар, Топбар)
        handleGlobalInput(mx, my, justTouched, isUa);

        // КРИТИЧНЕ ВИПРАВЛЕННЯ КРАШУ:
        // Якщо кнопка "Назад" щойно перемкнула екран (виклик switchMenu),
        // поточний екран був dispose(). Щоб не малювати знищеними шрифтами — негайно виходимо!
        if (mainGame.getScreen() != this) return;

        // Малювання базового UI
        drawUI(mx, my, isUa);

        // Обробка та малювання активної сторінки з її налаштуваннями
        if (!categories.isEmpty()) {
            SettingsCategory activeCat = categories.get(activeCategoryIndex);
            if (!activeCat.pages.isEmpty()) {
                SettingsPage activePage = activeCat.pages.get(activePageIndex);
                activePage.handleInput(mx, my, justTouched, isTouched);

                // Про всяк випадок, якщо якийсь елемент меню сам зробить switchMenu
                if (mainGame.getScreen() != this) return;

                activePage.render(batch, shapes, fontLarge, fontMedium, fontSmall, sidebarWidth, topBarHeight, isUa);
            }
        }
    }

    private void handleGlobalInput(float mx, float my, boolean justTouched, boolean isUa) {
        if (!justTouched) return;

        // Клік по кнопці "Назад"
        if (mx > 20 && mx < 20 + backBtnW && my > 20 && my < 20 + backBtnH) {
            switchMenu.popScreen();
            return;
        }

        // Обробка кліків по Сайдбару (Звук, Мова...)
        float catX = 20, catW = sidebarWidth - 40, catH = 50;
        float catStartY = WORLD_H - 100, catGap = 15;

        for (int i = 0; i < categories.size(); i++) {
            float y = catStartY - i * (catH + catGap);
            if (mx >= catX && mx <= catX + catW && my >= y && my <= y + catH) {
                if (activeCategoryIndex != i) {
                    activeCategoryIndex = i;
                    activePageIndex = 0; // Скидаємо вкладку на першу при зміні категорії
                }
                return;
            }
        }

        // Обробка кліків по Топбару (Музика, Звукові ефекти...)
        if (!categories.isEmpty()) {
            SettingsCategory activeCat = categories.get(activeCategoryIndex);
            float tabStartX = sidebarWidth + 30, tabEndX = WORLD_W - 30;
            float tabW = activeCat.pages.isEmpty() ? 0 : (tabEndX - tabStartX - (activeCat.pages.size() - 1) * 15) / activeCat.pages.size();
            float tabY = WORLD_H - topBarHeight + (topBarHeight - 45) / 2f;

            float currentTabX = tabStartX;
            for (int i = 0; i < activeCat.pages.size(); i++) {
                if (mx >= currentTabX && mx <= currentTabX + tabW && my >= tabY && my <= tabY + 45) {
                    activePageIndex = i;
                    return;
                }
                currentTabX += tabW + 15;
            }
        }
    }

    private void drawUI(float mx, float my, boolean isUa) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Панелі фону
        shapes.setColor(SIDEBAR_COLOR);
        shapes.rect(0, 0, sidebarWidth, WORLD_H);
        shapes.rect(sidebarWidth, WORLD_H - topBarHeight, WORLD_W - sidebarWidth, topBarHeight);

        // Кнопка Назад
        boolean backHovered = (mx > 20 && mx < 20 + backBtnW && my > 20 && my < 20 + backBtnH);
        drawRoundRect(20, 20, backBtnW, backBtnH, RADIUS, backHovered ? BTN_HOVER : Color.FIREBRICK);

        // Сайдбар
        float catX = 20, catW = sidebarWidth - 40, catH = 50;
        float catStartY = WORLD_H - 100, catGap = 15;
        for (int i = 0; i < categories.size(); i++) {
            boolean isActive = (i == activeCategoryIndex);
            float y = catStartY - i * (catH + catGap);
            boolean isHovered = (mx >= catX && mx <= catX + catW && my >= y && my <= y + catH);
            drawRoundRect(catX, y, catW, catH, RADIUS, isActive ? Color.DARK_GRAY : (isHovered ? BTN_HOVER : Color.BLACK));
        }

        // Топбар
        SettingsCategory activeCat = categories.get(activeCategoryIndex);
        float tabStartX = sidebarWidth + 30, tabEndX = WORLD_W - 30;
        float gap = 15, tabH = 45;
        float tabW = activeCat.pages.isEmpty() ? 0 : (tabEndX - tabStartX - (activeCat.pages.size() - 1) * gap) / activeCat.pages.size();
        float tabY = WORLD_H - topBarHeight + (topBarHeight - tabH) / 2f;

        float currentTabX = tabStartX;
        for (int i = 0; i < activeCat.pages.size(); i++) {
            boolean isActive = (i == activePageIndex);
            boolean isHovered = (mx >= currentTabX && mx <= currentTabX + tabW && my >= tabY && my <= tabY + tabH);
            drawRoundRect(currentTabX, tabY, tabW, tabH, RADIUS, isActive ? Color.DARK_GRAY : (isHovered ? BTN_HOVER : Color.BLACK));
            currentTabX += tabW + gap;
        }
        shapes.end();

        // Роздільні лінії
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BORDER_COLOR);
        shapes.rect(sidebarWidth, 0, 1, WORLD_H);
        shapes.rect(sidebarWidth, WORLD_H - topBarHeight, WORLD_W - sidebarWidth, 1);
        shapes.end();

        // Малювання текстів
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

    private enum SliderType {
        MUSIC,
        SFX
    }
}