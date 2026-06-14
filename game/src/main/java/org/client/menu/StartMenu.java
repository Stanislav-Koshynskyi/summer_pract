package org.client.menu;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
// ── LibGDX: ядро фреймворку ──────────────────────────────────────────────────
import com.badlogic.gdx.Gdx;                        // доступ до екрану, введення, файлів
import com.badlogic.gdx.Screen;                     // інтерфейс екрану LibGDX
import com.badlogic.gdx.graphics.Color;             // кольори (r, g, b, a)
import com.badlogic.gdx.graphics.OrthographicCamera; // 2D камера
import com.badlogic.gdx.graphics.g2d.BitmapFont;   // шрифт для малювання тексту
import com.badlogic.gdx.graphics.g2d.GlyphLayout;  // для вимірювання ширини тексту
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; // малює прямокутники/кола без текстур
import com.badlogic.gdx.utils.ScreenUtils;          // допоміжний метод очищення екрану
import com.badlogic.gdx.utils.viewport.FitViewport; // зберігає пропорції при зміні розміру вікна
import com.badlogic.gdx.utils.viewport.Viewport;
import org.core.enums.MenuStatus;

/**
 * Головне меню гри "One By One".
 *
 * Реалізує інтерфейс Screen — LibGDX сам викликатиме show(), render(),
 * resize(), hide(), dispose() у потрібний момент.
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * ЯК ПІДКЛЮЧИТИ ДО ПРОЕКТУ
 * ──────────────────────────────────────────────────────────────────────────────
 * 1. CoreGame повинен реалізовувати Game (не ApplicationAdapter):
 *
 *      public class CoreGame extends Game { ... }
 *
 * 2. У методі create() замість того щоб одразу завантажувати рівень,
 *    переключити на це меню:
 *
 *      @Override
 *      public void create() {
 *          setScreen(new StartMenu(this));   // показати меню при старті
 *      }
 *
 * 3. Файл StartMenu.java покласти в папку:
 *      game/src/main/java/org/client/menu/StartMenu.java
 * ──────────────────────────────────────────────────────────────────────────────
 */
public class StartMenu implements Screen {

    ///status menu
    private static final MenuStatus STATUS = MenuStatus.START_MENU;

    // ── Кольори (підібрані під скриншот) ─────────────────────────────────────
    /** Темно-сірий фон, як на скриншоті (#2E3240) */
    private static final Color BG_COLOR      = new Color(0x2e3240ff);

    /** Трохи світліший прямокутник кнопки (#3A3F52) */
    private static final Color BTN_COLOR     = new Color(0x3a3f52ff);

    /** Сіра рамка кнопки (#6B7280) */
    private static final Color BTN_BORDER    = new Color(0x6b7280ff);

    /** Підсвічування кнопки при наведенні миші */
    private static final Color BTN_HOVER     = new Color(0x4a5068ff);

    // ── Розміри та положення ──────────────────────────────────────────────────
    /** Логічна ширина сцени (пікселі). Viewport масштабує її під реальне вікно */
    private static final float WORLD_W   = 1280f;

    /** Логічна висота сцени */
    private static final float WORLD_H   = 720f;

    /** Ширина кнопки */
    private static final float BTN_W     = 440f;

    /** Висота кнопки */
    private static final float BTN_H     = 80f;

    /** Відстань між кнопками */
    private static final float BTN_GAP   = 20f;

    /** Товщина рамки кнопки */
    private static final float BORDER    = 3f;

    /** Радіус заокруглення кутів кнопки (у пікселях) */
    private static final int   RADIUS    = 18;

    // ── Підписи кнопок (за скриншотом) ───────────────────────────────────────
    private static final String[] LABELS = {
            "Почати гру",   // 0 — запускає рівень
            "Про авторів",  // 1 — екран з інформацією про авторів (поки заглушка)
            "Налаштування", // 2 — екран налаштувань (поки заглушка)
            "Правила"       // 3 — екран правил (поки заглушка)
    };

    // ── LibGDX об'єкти ────────────────────────────────────────────────────────
    /** Посилання на головну гру — потрібне для setScreen() */
    private final com.badlogic.gdx.Game game;

    /** Камера: визначає, яку частину сцени ми бачимо */
    private final OrthographicCamera camera;

    /**
     * Viewport підтримує логічні розміри WORLD_W×WORLD_H незалежно від
     * реального розміру вікна (letterboxing).
     */
    private final Viewport viewport;

    /** Малює прямокутники (фон кнопок, рамки) */
    private final ShapeRenderer shapes;

    /** Стандартний LibGDX шрифт — завантажується без зовнішніх файлів */
    private final BitmapFont font;

    /** Великий шрифт для заголовку */
    private final BitmapFont titleFont;

    /** SpriteBatch потрібен для малювання тексту через BitmapFont */
    private final com.badlogic.gdx.graphics.g2d.SpriteBatch batch;

    /** Допоміжний об'єкт для вимірювання розмірів тексту */
    private final GlyphLayout layout;

    // ── Координати кнопок ─────────────────────────────────────────────────────
    /** Масив X-координат лівого краю кожної кнопки */
    private final float[] btnX = new float[LABELS.length];

    /** Масив Y-координат нижнього краю кожної кнопки */
    private final float[] btnY = new float[LABELS.length];

    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Конструктор: отримує посилання на Game, щоб мати змогу
     * перемикати екрани через game.setScreen(...).
     *
     * @param game головний клас гри (CoreGame)
     */
    // 1. Додаємо масив статусів, що відповідають кнопкам (порядок такий самий, як у LABELS)
    private static final MenuStatus[] BUTTON_TARGETS = {
            MenuStatus.PLAY_GAME_MENU,      // 0 — Start game (або ігровий екран)
            MenuStatus.ABOUT_AUTHORS_MENU,  // 1 — About authors
            MenuStatus.SETTINGS_MENU,       // 2 — Settings
            MenuStatus.RULES_MENU           // 3 — Rules
    };

    // 2. Зберігаємо посилання на SwitchMenu
    private final SwitchMenu switchMenu;

    // 3. Оновлюємо конструктор, щоб він приймав SwitchMenu
    public StartMenu(com.badlogic.gdx.Game game, SwitchMenu switchMenu) {
        this.game = game;
        this.switchMenu = switchMenu;
        // Створюємо 2D камеру та прив'язуємо FitViewport до логічних розмірів
        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);

        // Налаштовуємо камеру: (0,0) = ліво-знизу, (WORLD_W, WORLD_H) = право-зверху
        camera.setToOrtho(false, WORLD_W, WORLD_H);

        shapes    = new ShapeRenderer();
        batch     = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        layout    = new GlyphLayout();

        // ── Шрифт заголовку ───────────────────────────────────────────────────
        // BitmapFont() без аргументів завантажує вбудований Arial 15px.
        // Масштабуємо його через setScale, щоб виглядало як великий заголовок.
        // Замість старого titleFont = new BitmapFont(); та font = new BitmapFont(); робимо так:
        if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Додаємо набір українських літер
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

            // 1. Великий шрифт для головного заголовку "One By One"
            parameter.size = 70;
            titleFont = generator.generateFont(parameter);
            titleFont.setColor(Color.WHITE);

            // 2. Середній шрифт для кнопок меню
            parameter.size = 32;
            font = generator.generateFont(parameter);
            font.setColor(Color.WHITE);

            generator.dispose(); // Звільняємо пам'ять генератора (генератор більше не потрібен)
        } else {
            // Фолбек (запасний варіант), якщо файл шрифту кудись зник
            titleFont = new BitmapFont();
            titleFont.getData().setScale(5f);
            font = new BitmapFont();
            font.getData().setScale(2.8f);
            Gdx.app.error("FontError", "Шрифт fonts/Roboto-Regular.ttf не знайдено! Українська мова не працюватиме.");
        }

        // ── Розрахунок позицій кнопок ─────────────────────────────────────────
        // Кнопки розміщуємо по центру по горизонталі.
        // Починаємо знизу вгору: перша кнопка трохи нижче центру.
        float totalHeight = LABELS.length * BTN_H + (LABELS.length - 1) * BTN_GAP;
        float startY      = (WORLD_H - totalHeight) / 2f - 30f; // трохи нижче центру

        for (int i = 0; i < LABELS.length; i++) {
            btnX[i] = (WORLD_W - BTN_W) / 2f;   // центрування по X
            // Перша кнопка (i=0) — найвища; рахуємо зверху вниз
            btnY[i] = startY + (LABELS.length - 1 - i) * (BTN_H + BTN_GAP);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Методи інтерфейсу Screen
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * show() викликається LibGDX, коли цей Screen стає активним.
     * Тут нічого ініціалізовувати не потрібно — все вже у конструкторі.
     */
    @Override
    public void show() { /* нічого */ }

    /**
     * render() — головний цикл; викликається ~60 разів на секунду.
     *
     * @param delta час (секунди) з минулого кадру
     */
    @Override
    public void render(float delta) {
        // 1. Очищаємо екран кольором фону
        ScreenUtils.clear(BG_COLOR);

        // 🟢 ЗАКЛИНАННЯ ВІД ВИЛІТУ:
        // Якщо після кліку active screen став іншим (не цим класом),
        // ми НЕГАЙНО виходимо з методу і не чіпаємо shapeRenderer/batch!
        if (game.getScreen() != this) return;

        // 2. Оновлюємо камеру перед малюванням
        camera.update();

        // ── Фази малювання ────────────────────────────────────────────────────
        // ShapeRenderer і SpriteBatch НЕ можна використовувати одночасно —
        // кожен із них має викликати begin()/end() окремо.

        drawButtons();   // фаза 1: прямокутники (ShapeRenderer)
        drawText();      // фаза 2: текст (SpriteBatch + BitmapFont)
        handleInput();   // фаза 3: обробка кліків миші
    }

    /**
     * Малює фон кнопок та їхні рамки за допомогою ShapeRenderer.
     *
     * ShapeRenderer.ShapeType.Filled — суцільна заливка.
     * ShapeRenderer.ShapeType.Line   — тільки контур.
     */
    private void drawButtons() {
        shapes.setProjectionMatrix(camera.combined); // прив'язуємо до камери

        // ── Заливка кнопок ────────────────────────────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < LABELS.length; i++) {
            // Підсвічування якщо миша над кнопкою
            boolean hovered = isMouseOver(i);
            shapes.setColor(hovered ? BTN_HOVER : BTN_COLOR);

            // Малюємо прямокутник із заокругленими кутами
            // LibGDX не має вбудованого roundRect, тому малюємо центральний
            // прямокутник + два бічних + чотири кола по кутах.
            drawRoundRect(btnX[i], btnY[i], BTN_W, BTN_H, RADIUS, hovered ? BTN_HOVER : BTN_COLOR);
        }

        shapes.end();

        // ── Рамки кнопок ──────────────────────────────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BTN_BORDER);

        // Товщину лінії встановлюємо через OpenGL
        Gdx.gl.glLineWidth(BORDER);

        for (int i = 0; i < LABELS.length; i++) {
            drawRoundRectOutline(btnX[i], btnY[i], BTN_W, BTN_H, RADIUS);
        }

        shapes.end();
        Gdx.gl.glLineWidth(1f); // повертаємо стандартну товщину
    }

    /**
     * Малює заокруглений прямокутник через набір примітивів.
     * LibGDX не має готового методу, тому ручна реалізація.
     *
     * @param x      лівий край
     * @param y      нижній край
     * @param w      ширина
     * @param h      висота
     * @param r      радіус кута
     * @param color  колір заливки
     */
    private void drawRoundRect(float x, float y, float w, float h, int r, Color color) {
        shapes.setColor(color);
        // Центральний прямокутник
        shapes.rect(x + r, y, w - 2 * r, h);
        // Лівий та правий прямокутники
        shapes.rect(x, y + r, r, h - 2 * r);
        shapes.rect(x + w - r, y + r, r, h - 2 * r);
        // Чотири кути — кола
        shapes.circle(x + r,         y + r,         r, 20);
        shapes.circle(x + w - r,     y + r,         r, 20);
        shapes.circle(x + r,         y + h - r,     r, 20);
        shapes.circle(x + w - r,     y + h - r,     r, 20);
    }

    /**
     * Малює контур заокругленого прямокутника (лінії + дуги).
     */
    private void drawRoundRectOutline(float x, float y, float w, float h, int r) {
        // Горизонтальні лінії
        shapes.line(x + r,     y,         x + w - r, y);         // низ
        shapes.line(x + r,     y + h,     x + w - r, y + h);     // верх
        // Вертикальні лінії
        shapes.line(x,         y + r,     x,         y + h - r); // ліво
        shapes.line(x + w,     y + r,     x + w,     y + h - r); // право
        // Кути — дуги по 90°
        shapes.arc(x + r,         y + r,         r, 180, 90, 12);
        shapes.arc(x + w - r,     y + r,         r, 270, 90, 12);
        shapes.arc(x + r,         y + h - r,     r,  90, 90, 12);
        shapes.arc(x + w - r,     y + h - r,     r,   0, 90, 12);
    }

    /**
     * Малює заголовок "One By One" та підписи кнопок.
     *
     * BitmapFont.draw() потребує активного SpriteBatch.
     */
    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // ── Заголовок ─────────────────────────────────────────────────────────
        String title = "One By One";
        layout.setText(titleFont, title);                   // вимірюємо ширину тексту

        float titleX = (WORLD_W - layout.width) / 2f;      // центруємо по X
        float titleY = btnY[0] + BTN_H + layout.height + 60f; // над першою кнопкою

        titleFont.draw(batch, title, titleX, titleY);

        // ── Підписи кнопок ────────────────────────────────────────────────────
        for (int i = 0; i < LABELS.length; i++) {
            layout.setText(font, LABELS[i]);

            // Центруємо текст усередині кнопки
            float textX = btnX[i] + (BTN_W - layout.width)  / 2f;
            float textY = btnY[i] + (BTN_H + layout.height) / 2f; // вертикальний центр

            font.draw(batch, LABELS[i], textX, textY);
        }

        batch.end();
    }

    /**
     * Перевіряє клік мишею по кнопках.
     *
     * justTouched() повертає true тільки в момент натиснення (не утримання),
     * тому подія спрацює один раз.
     */
    private void handleInput() {
        if (!Gdx.input.justTouched()) return; // нічого не натиснуто — виходимо

        // Переводимо координати миші (екранні, Y донизу) у логічні (Y догори)
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY(); // інвертуємо Y

        // Viewport може додавати letterbox-відступи — враховуємо їх
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth())  * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;

        for (int i = 0; i < LABELS.length; i++) {
            if (wx >= btnX[i] && wx <= btnX[i] + BTN_W &&
                    wy >= btnY[i] && wy <= btnY[i] + BTN_H) {
                onButtonClick(i); // кнопку натиснуто!
                return;
            }
        }
    }

    /**
     * Викликається при натисканні кнопки з індексом {@code index}.
     *
     * @param index індекс кнопки (0 = "Почати гру", 1 = "Про авторів", ...)
     */
    private void onButtonClick(int index) {
        if (index >= 0 && index < BUTTON_TARGETS.length) {
            Gdx.app.log("StartMenu", "Натиснуто кнопку: " + LABELS[index] + " -> Перехід до " + BUTTON_TARGETS[index]);

            // Перемикаємо меню через наш OCP менеджер
            switchMenu.switchMenu(BUTTON_TARGETS[index]);
        }
    }

    /**
     * Перевіряє, чи знаходиться курсор миші над кнопкою з індексом {@code i}.
     * Використовується для hover-підсвічування.
     *
     * @param i індекс кнопки
     * @return true якщо миша над кнопкою
     */
    private boolean isMouseOver(int i) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth())  * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;

        return wx >= btnX[i] && wx <= btnX[i] + BTN_W &&
                wy >= btnY[i] && wy <= btnY[i] + BTN_H;
    }

    /**
     * resize() — LibGDX викликає цей метод при зміні розміру вікна.
     * FitViewport сам перераховує letterbox, нам треба лише передати нові розміри.
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true = центрує камеру
    }

    /** pause() / resume() — потрібні для мобільних платформ, тут не використовуються */
    @Override public void pause()  { }
    @Override public void resume() { }

    /**
     * hide() — викликається, коли цей Screen перестає бути активним
     * (наприклад, при переході до рівня).
     */
    @Override
    public void hide() { }

    /**
     * dispose() — звільняє пам'ять GPU.
     * LibGDX не вивільняє ресурси автоматично — обов'язково викликати dispose()!
     */
    @Override
    public void dispose() {
        shapes.dispose();    // ShapeRenderer тримає буфер GPU
        batch.dispose();     // SpriteBatch теж
        font.dispose();      // BitmapFont тримає текстуру атласу
        titleFont.dispose();
    }
}