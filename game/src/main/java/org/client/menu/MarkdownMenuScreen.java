package org.client.menu;

// Імпорт необхідних класів двигуна LibGDX для роботи з графікою, вікнами та введенням
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import org.core.enums.MenuStatus;

import java.util.ArrayList;
import java.util.List;

public class MarkdownMenuScreen implements Screen {

    // Перелічення (Enum) для ідентифікації типів Markdown елементів
    private enum ElementType { H1, H2, H4, H5, H6, PARAGRAPH, IMAGE }

    // Структурний клас для представлення окремого елемента контенту (текст або картинка)
    private static class MenuElement {
        ElementType type;  // Тип елемента
        String text;       // Текстовий вміст (якщо є)
        Texture texture;   // Текстура зображення (якщо тип IMAGE)
    }

    // Клас для представлення вкладки, яка створюється за допомогою тегу ###
    private static class Tab {
        String title;                         // Назва вкладки, яка пишеться на кнопці
        List<MenuElement> elements = new ArrayList<>(); // Список елементів всередині цієї вкладки
    }

    // Системні змінні LibGDX для екранів, рендерингу та камер
    private final Game game;
    private final SwitchMenu switchMenu;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private BitmapFont font; // Шрифт робимо динамічним для генерації кирилиці
    private final GlyphLayout glyphLayout;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    // Списки для збереження результатів парсингу файлу
    private final List<MenuElement> globalElements = new ArrayList<>(); // Елементи ДО першого ### (показуються завжди)
    private final List<Tab> tabs = new ArrayList<>();                   // Список створених вкладок (###)
    private int activeTabIndex = 0;                                     // Індекс поточної вибраної вкладки
    private final List<Texture> texturesToDispose = new ArrayList<>();  // Список текстур для очищення пам'яті GPU

    // Фіксовані розміри нашої віртуальної системи координат (Логічний екран)
    private static final float WORLD_W = 1280;
    private static final float WORLD_H = 720;

    // Налаштування розмірів та координат для верхньої фіксованої кнопки "BACK"
    private final float backBtnX = 50;
    private final float backBtnY = WORLD_H - 100;
    private final float backBtnW = 150;
    private final float backBtnH = 50;

    // Змінні для керування вертикальним прокручуванням (Скролінгом)
    private float scrollOffsetY = 0;       // На скільки пікселів контент зміщений вгору
    private float maxScrollY = 0;          // Максимальна межа скролінгу (обчислюється динамічно)
    private float totalContentHeight = 0;  // Загальна висота всього тексту в пікселях

    // Змінні для правого бічного повзунка (Скролбару)
    private final float scrollbarX = WORLD_W - 30; // Координата X для вертикальної смуги
    private final float scrollbarW = 12;           // Ширина повзунка
    private boolean isDraggingScrollbar = false;   // Прапорець: чи затиснув користувач повзунок мишкою

    // Конструктор екрану
    public MarkdownMenuScreen(Game game, SwitchMenu switchMenu, String markdownFilePath) {
        this.game = game;
        this.switchMenu = switchMenu;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_W, WORLD_H, camera);

        // Викликаємо метод створення шрифту з підтримкою української мови
        initUkrainianFont();

        // Читаємо та розбираємо переданий markdown файл
        parseMarkdownFile(markdownFilePath);

        // Розраховуємо висоту вмісту, щоб знати межі прокручування
        calculateContentHeight();
    }

    // Метод для динамічної генерації шрифту з підтримкою українських літер
    private void initUkrainianFont() {
        try {
            // Перевіряємо, чи поклав користувач файл шрифту в ресурси
            if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

                // Перераховуємо всі символи, які генератор має вбудувати в текстуру шрифту (Англійські + Українські)
                parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";
                parameter.size = 22; // Оптимальний розмір тексту

                this.font = generator.generateFont(parameter); // Створюємо готовий BitmapFont
                generator.dispose(); // Обов'язково знищуємо генератор, щоб звільнити RAM
            } else {
                // Якщо файлу немає — створюємо стандартний дефолтний (буде лише англійська мова)
                this.font = new BitmapFont();
                Gdx.app.error("FontError", "Файл fonts/Roboto-Regular.ttf не знайдено! Українська мова не працюватиме.");
            }
        } catch (Exception e) {
            this.font = new BitmapFont();
            Gdx.app.error("FontError", "Помилка при ініціалізації FreeTypeFont.", e);
        }
    }

    // Метод парсингу Markdown файлу line-by-line
    private void parseMarkdownFile(String path) {
        try {
            // Читаємо весь файл у кодуванні UTF-8
            String content = Gdx.files.internal(path).readString("UTF-8");
            // Ділимо текст на масив рядків по символу перенесення
            String[] lines = content.split("\\r?\\n");

            // Змінна для відстеження поточної вкладки. Спочатку її немає (null)
            Tab currentTab = null;

            for (String line : lines) {
                line = line.trim(); // Прибираємо зайві пробіли на початку й кінці рядка
                if (line.isEmpty()) continue; // Пропускаємо порожні рядки

                // Якщо знайшли маркер "### ", створюємо нову вкладку
                if (line.startsWith("### ")) {
                    currentTab = new Tab();
                    currentTab.title = line.substring(4).trim(); // Записуємо текст після "### " як назву вкладки
                    tabs.add(currentTab); // Додаємо вкладку у загальний список
                    continue; // Переходимо до наступного рядка файлу
                }

                // Створюємо новий елемент контенту
                MenuElement element = new MenuElement();

                // Перевірка на стандартні Markdown заголовки за допомогою префіксів рядка
                if (line.startsWith("# ")) {
                    element.type = ElementType.H1;
                    element.text = line.substring(2);
                } else if (line.startsWith("## ")) {
                    element.type = ElementType.H2;
                    element.text = line.substring(3);
                } else if (line.startsWith("#### ")) {
                    element.type = ElementType.H4;
                    element.text = line.substring(5);
                } else if (line.startsWith("##### ")) {
                    element.type = ElementType.H5;
                    element.text = line.substring(6);
                } else if (line.startsWith("###### ")) {
                    element.type = ElementType.H6;
                    element.text = line.substring(7);
                } else if (line.startsWith("![") && line.contains("](") && line.endsWith(")")) {
                    // Парсинг зображення виду: ![Опис](шлях/до/картинки.png)
                    element.type = ElementType.IMAGE;
                    int startIdx = line.indexOf("](") + 2;
                    int endIdx = line.length() - 1;
                    String imgPath = line.substring(startIdx, endIdx).trim();

                    // Перевіряємо, чи файл зображення фізично існує в ресурсах Maven
                    if (Gdx.files.internal(imgPath).exists()) {
                        Texture tex = new Texture(Gdx.files.internal(imgPath));
                        element.texture = tex;
                        texturesToDispose.add(tex); // Додаємо у список для видалення з відеопам'яті пізніше
                    } else {
                        // Якщо картинку не знайдено, перетворюємо елемент на текст із помилкою
                        element.type = ElementType.PARAGRAPH;
                        element.text = "[Помилка: Зображення '" + imgPath + "' відсутнє у ресурсах]";
                    }
                } else {
                    // Якщо рядок не підійшов під жоден опис — це звичайний абзац тексту
                    element.type = ElementType.PARAGRAPH;
                    element.text = line;
                }

                // Логіка додавання елемента:
                // Якщо currentTab == null, значить ми ЩЕ НЕ зустріли жодного "### ".
                // За вашою вимогою, додаємо такий текст у globalElements (показується на екрані без вкладок відразу)
                if (element.text != null || element.texture != null) {
                    if (currentTab == null) {
                        globalElements.add(element);
                    } else {
                        currentTab.elements.add(element);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MarkdownParser", "Помилка читання файлу: " + path, e);
        }
    }

    // Метод динамічного підрахунку висоти всього контенту на екрані
    private void calculateContentHeight() {
        totalContentHeight = 0; // Скидаємо лічильник висоти

        // Створюємо загальний тимчасовий список елементів, які будуть намальовані в даний момент
        List<MenuElement> activeElements = new ArrayList<>(globalElements); // Спочатку додаємо глобальний текст
        if (!tabs.isEmpty() && activeTabIndex < tabs.size()) {
            activeElements.addAll(tabs.get(activeTabIndex).elements); // Потім додаємо текст активної вкладки
        }

        // Проходимо по кожному елементу та додаємо його висоту у пікселях
        for (MenuElement element : activeElements) {
            switch (element.type) {
                case H1: totalContentHeight += 60; break;
                case H2: totalContentHeight += 45; break;
                case H4: totalContentHeight += 40; break;
                case H5: totalContentHeight += 35; break;
                case H6: totalContentHeight += 30; break;
                case PARAGRAPH:
                    font.getData().setScale(1.1f);
                    // Використовуємо glyphLayout для розрахунку висоти тексту з урахуванням автоматичного переносу рядків
                    glyphLayout.setText(font, element.text, Color.WHITE, WORLD_W - 200, 8, true);
                    totalContentHeight += (glyphLayout.height + 25); // Висота блоку + відступ нижній
                    break;
                case IMAGE:
                    if (element.texture != null) {
                        totalContentHeight += element.texture.getHeight() + 30; // Висота картинки + відступ
                    }
                    break;
            }
        }

        // Визначаємо зону перегляду (Висота екрана мінус висота фіксованого заголовка меню)
        float viewAreaHeight = WORLD_H - 180;
        // Максимальний скрол — це різниця між усім контентом і видимим вікном
        maxScrollY = Math.max(0, totalContentHeight - viewAreaHeight);

        // Коригуємо скрол, якщо він вийшов за межі після перемикання вкладок
        if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
        if (scrollOffsetY < 0) scrollOffsetY = 0;
    }

    @Override
    public void show() {
        // Реєструємо InputProcessor для відстеження обертання коліщатка миші
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                // amountY дорівнює 1 (скрол вниз) або -1 (скрол вгору)
                scrollOffsetY += amountY * 45; // Прокручуємо на 45 пікселів за один клік коліщатка

                // Перевірка меж
                if (scrollOffsetY < 0) scrollOffsetY = 0;
                if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
                return true;
            }
        });
    }

    // Допоміжний метод обробки натискань миші, клавіатури та перетягування повзунка
    private void handleInput(float wx, float wy) {
        // 1. Скролінг стрілочками на клавіатурі (Вгору / Вниз)
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scrollOffsetY -= 300 * Gdx.graphics.getDeltaTime(); // Швидкість руху вгору
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scrollOffsetY += 300 * Gdx.graphics.getDeltaTime(); // Швидкість руху вниз
        }

        // 2. Логіка кліків мишкою (justTouched спрацьовує один раз при натисканні)
        if (Gdx.input.justTouched()) {
            // Клік по кнопці BACK
            if (wx >= backBtnX && wx <= backBtnX + backBtnW && wy >= backBtnY && wy <= backBtnY + backBtnH) {
                switchMenu.switchMenu(MenuStatus.START_MENU);
                return;
            }

            // Перевірка кліків по кнопках вкладок нагорі
            float tabX = backBtnX + backBtnW + 50;
            float tabY = WORLD_H - 100;
            float tabH = 50;

            for (int i = 0; i < tabs.size(); i++) {
                font.getData().setScale(1.2f);
                glyphLayout.setText(font, tabs.get(i).title);
                float tabW = glyphLayout.width + 40;

                // Якщо клікнули на вкладку — робимо її активною та скидаємо скрол до верху
                if (wx >= tabX && wx <= tabX + tabW && wy >= tabY && wy <= tabY + tabH) {
                    activeTabIndex = i;
                    scrollOffsetY = 0;
                    calculateContentHeight(); // Перераховуємо висоту для нової статті
                    break;
                }
                tabX += tabW + 10;
            }

            // Перевірка натискання на правий бігунок скролбару
            float scrollbarH = 60; // Висота повзунка
            float scrollTrackH = WORLD_H - 220; // Висота доріжки, по якій ходить повзунок
            // Формула поточної позиції Y повзунка на екрані
            float scrollThumbY = (WORLD_H - 180) - scrollbarH - ((scrollOffsetY / maxScrollY) * (scrollTrackH - scrollbarH));

            // Якщо мишка потрапила у межі прямокутника повзунка — вмикаємо режим перетягування (Drag)
            if (maxScrollY > 0 && wx >= scrollbarX && wx <= scrollbarX + scrollbarW && wy >= scrollThumbY && wy <= scrollThumbY + scrollbarH) {
                isDraggingScrollbar = true;
            }
        }

        // 3. Логіка утримання та перетягування повзунка (Drag-and-Drop скролінг)
        if (!Gdx.input.isTouched()) {
            isDraggingScrollbar = false; // Якщо кнопку миші відпустили — вимикаємо режим утримання
        } else if (isDraggingScrollbar && maxScrollY > 0) {
            float scrollTrackH = WORLD_H - 220;
            // Вираховуємо відносну позицію миші на доріжці скролбару та конвертуємо у відсотки скролу контенту
            float relativeY = (WORLD_H - 180 - 30) - wy;
            float percentage = relativeY / scrollTrackH;
            scrollOffsetY = percentage * maxScrollY;
        }

        // Захист: тримаємо скролінг строго в межах від 0 до maxScrollY
        if (scrollOffsetY < 0) scrollOffsetY = 0;
        if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
    }

    @Override
    public void render(float delta) {
        // Очищаємо екран темно-синім матовим кольором щокадру
        ScreenUtils.clear(0.1f, 0.11f, 0.15f, 1f);

        // Отримання координат миші та переведення їх у нашу віртуальну систему WORLD_W / WORLD_H (FitViewport)
        float mx = Gdx.input.getX();
        float my = WORLD_H - Gdx.input.getY();
        float scaleX = WORLD_W / (float) viewport.getScreenWidth();
        float scaleY = WORLD_H / (float) viewport.getScreenHeight();
        float wx = (mx - viewport.getLeftGutterWidth()) * scaleX;
        float wy = (my - viewport.getBottomGutterHeight()) * scaleY;

        // Викликаємо обробку введення (Клавіші + Миша)
        handleInput(wx, wy);

        if (game.getScreen() != this) return;

        // Оновлюємо камеру та передаємо матриці проекції у рендерери
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // =========================================================================
        // БЛОК 1: МАЛЮВАННЯ ФІКСОВАНОГО МЕНЮ (HEADER), ЯКЕ НЕ РУХАЄТЬСЯ (STICKY)
        // =========================================================================
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Малюємо фон кнопки BACK (Темно-червоний)
        shapeRenderer.setColor(Color.FIREBRICK);
        shapeRenderer.rect(backBtnX, backBtnY, backBtnW, backBtnH);

        // Малюємо фонові прямокутники для кнопок вкладок нагорі (якщо вкладки є у файлі)
        float currentTabX = backBtnX + backBtnW + 50;
        for (int i = 0; i < tabs.size(); i++) {
            font.getData().setScale(1.2f);
            glyphLayout.setText(font, tabs.get(i).title);
            float tabW = glyphLayout.width + 40; // Ширина кнопки залежить від довжини тексту назви

            // Якщо вкладка активна — фарбуємо в темно-сірий колір, інакше в чорний
            if (i == activeTabIndex) shapeRenderer.setColor(Color.DARK_GRAY);
            else shapeRenderer.setColor(Color.BLACK);

            shapeRenderer.rect(currentTabX, backBtnY, tabW, backBtnH);
            currentTabX += tabW + 10; // Зсуваємо координату X для наступної кнопки вкладки
        }

        // Малюємо бічний скролбар (якщо контент не поміщається на екрані)
        if (maxScrollY > 0) {
            float scrollbarH = 60; // Висота бігунка повзунка
            float scrollTrackH = WORLD_H - 220; // Висота доріжки
            // Рахуємо динамічну позицію бігунка по Y на основі відсотка поточного скролу
            float scrollThumbY = (WORLD_H - 180) - scrollbarH - ((scrollOffsetY / maxScrollY) * (scrollTrackH - scrollbarH));

            // Малюємо підкладку (Доріжку) скролбару сірим кольором
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(scrollbarX, 30, scrollbarW, WORLD_H - 210);

            // Малюємо сам повзунок (Якщо тримаємо мишкою — золотий колір, якщо ні — світло-сірий)
            shapeRenderer.setColor(isDraggingScrollbar ? Color.GOLD : Color.LIGHT_GRAY);
            shapeRenderer.rect(scrollbarX, scrollThumbY, scrollbarW, scrollbarH);
        }
        shapeRenderer.end();

        // Малюємо текст поверх кнопок меню
        batch.begin();
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        glyphLayout.setText(font, "BACK");
        // Малюємо слово BACK чітко по центру червоної кнопки
        font.draw(batch, "BACK", backBtnX + (backBtnW - glyphLayout.width) / 2, backBtnY + (backBtnH + glyphLayout.height) / 2);

        currentTabX = backBtnX + backBtnW + 50;
        for (int i = 0; i < tabs.size(); i++) {
            font.getData().setScale(1.2f);
            // Активний текст робимо золотим, неактивний — сірим
            font.setColor(i == activeTabIndex ? Color.GOLD : Color.LIGHT_GRAY);
            glyphLayout.setText(font, tabs.get(i).title);
            font.draw(batch, tabs.get(i).title, currentTabX + 20, backBtnY + (backBtnH + glyphLayout.height) / 2);
            currentTabX += glyphLayout.width + 40 + 10;
        }
        batch.end();

        // =========================================================================
        // БЛОК 2: МАЛЮВАННЯ КОНТЕНТУ З ЕФЕКТОМ СКРОЛІНГУ ТА ОБРІЗАННЯМ (SCISSOR TEST)
        // =========================================================================

        // Вмикаємо OpenGL Scissor Test. Він дозволяє обрізати все, що виходить за межі вказаного прямокутника
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        // Перераховуємо логічну зону обрізання тексту (нижче ніж WORLD_H - 180) у реальні пікселі вікна монітора
        float scissorX = viewport.getLeftGutterWidth();
        float scissorY = viewport.getBottomGutterHeight();
        float scissorW = viewport.getScreenWidth();
        float scissorH = viewport.getScreenHeight() * ((WORLD_H - 175f) / WORLD_H); // Виключаємо верхні 175 логічних пікселів меню

        // Активуємо маску обрізання в OpenGL
        Gdx.gl.glScissor((int) scissorX, (int) scissorY, (int) scissorW, (int) scissorH);

        batch.begin();

        // Початкова координата Y для рендерингу зміщується ВГОРУ на величину скролу scrollOffsetY
        float currentY = (WORLD_H - 180) + scrollOffsetY;
        float leftMargin = 100; // Лівий відступ від краю екрана для красивого вигляду

        // Зшиваємо елементи до купи: спочатку глобальний текст файлу, потім текст вкладки
        List<MenuElement> elementsToRender = new ArrayList<>(globalElements);
        if (!tabs.isEmpty() && activeTabIndex < tabs.size()) {
            elementsToRender.addAll(tabs.get(activeTabIndex).elements);
        }

        // Цикл рендерингу контенту залежно від типу тегу
        for (MenuElement element : elementsToRender) {
            switch (element.type) {
                case H1:
                    font.getData().setScale(2.5f); // Великий шрифт
                    font.setColor(Color.GOLD);
                    glyphLayout.setText(font, element.text);
                    // Малюємо по центру екрана
                    font.draw(batch, element.text, (WORLD_W - glyphLayout.width) / 2, currentY);
                    currentY -= 60; // Зсуваємо курсор вниз на висоту заголовка
                    break;
                case H2:
                    font.getData().setScale(1.8f);
                    font.setColor(Color.SKY);
                    font.draw(batch, element.text, leftMargin, currentY);
                    currentY -= 45;
                    break;
                case H4:
                    font.getData().setScale(1.5f);
                    font.setColor(Color.ORANGE);
                    font.draw(batch, element.text, leftMargin, currentY);
                    currentY -= 40;
                    break;
                case H5:
                    font.getData().setScale(1.3f);
                    font.setColor(Color.LIME);
                    font.draw(batch, element.text, leftMargin, currentY);
                    currentY -= 35;
                    break;
                case H6:
                    font.getData().setScale(1.1f);
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, element.text, leftMargin, currentY);
                    currentY -= 30;
                    break;
                case PARAGRAPH:
                    font.getData().setScale(1.1f);
                    font.setColor(Color.WHITE);
                    // Функція автоперенесення тексту: вказуємо ширину WORLD_W - 200, прапорець wrap = true
                    font.draw(batch, element.text, leftMargin, currentY, WORLD_W - 200, 8, true);
                    glyphLayout.setText(font, element.text, Color.WHITE, WORLD_W - 200, 8, true);
                    currentY -= (glyphLayout.height + 25); // Розраховуємо динамічний крок вниз по висоті отриманого абзацу
                    break;
                case IMAGE:
                    if (element.texture != null) {
                        float imgW = element.texture.getWidth();
                        float imgH = element.texture.getHeight();
                        float imgX = (WORLD_W - imgW) / 2; // Центруємо картинку по горизонталі
                        currentY -= imgH; // Картинки в LibGDX малюються знизу-вгору, тому спочатку віднімаємо висоту
                        batch.draw(element.texture, imgX, currentY);
                        currentY -= 30; // Додатковий відступ знизу картинки
                    }
                    break;
            }
        }
        batch.end();

        // Обов'язково вимикаємо Scissor Test, щоб він не обрізав графіку на інших екранах гри
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        // При виході з екрана знімаємо наш процесор введення, щоб він не заважав іншим екранам гри
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        // Очищення важких нативних ресурсів GPU для запобігання витоку пам'яті (Memory Leak)
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        for (Texture texture : texturesToDispose) texture.dispose();
    }
}