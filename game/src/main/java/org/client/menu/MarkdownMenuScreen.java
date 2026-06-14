package org.client.menu;

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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.client.MainGame;
import org.core.enums.MenuStatus;
import org.core.enums.LanguageUI;

import java.util.ArrayList;
import java.util.List;

public class MarkdownMenuScreen implements Screen {

    private enum ElementType { H2, H4, H5, H6, PARAGRAPH, IMAGE }

    private static class MenuElement {
        ElementType type;
        String text;
        Texture texture;
    }

    private static class Tab {
        String title;
        List<MenuElement> elements = new ArrayList<>();
    }

    private final Game game;
    private final SwitchMenu switchMenu;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final List<MenuElement> globalElements = new ArrayList<>();
    private final List<Tab> tabs = new ArrayList<>();
    private int activeTabIndex = 0;
    private final List<Texture> texturesToDispose = new ArrayList<>();

    // Геометрія віртуального світу
    private static final float WORLD_W = 1280;
    private static final float WORLD_H = 720;

    // Налаштування верхньої панелі та кнопок
    private final float backBtnX = 50;
    private final float backBtnY = WORLD_H - 160;
    private final float backBtnW = 140;
    private final float backBtnH = 60; // Збільшили висоту з 45 до 60, щоб вмістити 2 рядки тексту

    // Чіткі межі вікна прокручування контенту
    private final float contentLeft = 100;
    private final float contentWidth = WORLD_W - 230;
    private final float contentTop = backBtnY - 25;
    private final float contentBottom = 40;
    private final float contentHeight = contentTop - contentBottom;

    private String h1Title = "";

    private float scrollOffsetY = 0;
    private float maxScrollY = 0;
    private float totalContentHeight = 0;

    private final float scrollbarX = WORLD_W - 40;
    private final float scrollbarW = 10;
    private boolean isDraggingScrollbar = false;
    private LanguageUI language;

    public MarkdownMenuScreen(Game game, SwitchMenu switchMenu, String baseFilePath, LanguageUI language) {
        this.game = game;
        this.switchMenu = switchMenu;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.glyphLayout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        this.language = language;
        initUkrainianFont();
        parseMarkdownFile(baseFilePath, language);
        calculateContentHeight();
    }

    private void initUkrainianFont() {
        try {
            if (Gdx.files.internal("fonts/Roboto-Regular.ttf").exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

                parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзиіїйклмнопрстуфхцчшщьюя";
                parameter.size = 22;

                this.font = generator.generateFont(parameter);
                generator.dispose();
            } else {
                this.font = new BitmapFont();
                Gdx.app.error("FontError", "Шрифт не знайдено.");
            }
        } catch (Exception e) {
            this.font = new BitmapFont();
            Gdx.app.error("FontError", "Помилка ініціалізації.", e);
        }
    }

    private void parseMarkdownFile(String baseFilePath, LanguageUI language) {
        String suffix = (language == LanguageUI.ENGLISH) ? "_en.md" : "_uk.md";
        String path = baseFilePath + suffix;

        try {
            String content = Gdx.files.internal(path).readString("UTF-8");
            String[] lines = content.split("\\r?\\n");

            Tab currentTab = null;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("### ")) {
                    currentTab = new Tab();
                    currentTab.title = line.substring(4).trim();
                    tabs.add(currentTab);
                    continue;
                }

                if (line.startsWith("# ")) {
                    h1Title = line.substring(2).trim();
                    continue;
                }

                MenuElement element = new MenuElement();

                if (line.startsWith("## ")) {
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
                    element.type = ElementType.IMAGE;
                    int startIdx = line.indexOf("](") + 2;
                    int endIdx = line.length() - 1;
                    String imgPath = line.substring(startIdx, endIdx).trim();

                    if (Gdx.files.internal(imgPath).exists()) {
                        Texture tex = new Texture(Gdx.files.internal(imgPath));
                        element.texture = tex;
                        texturesToDispose.add(tex);
                    } else {
                        element.type = ElementType.PARAGRAPH;
                        element.text = "[Помилка: Зображення '" + imgPath + "' відсутнє]";
                    }
                } else {
                    element.type = ElementType.PARAGRAPH;
                    element.text = line;
                }

                if (element.text != null || element.texture != null) {
                    if (currentTab == null) {
                        globalElements.add(element);
                    } else {
                        currentTab.elements.add(element);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MarkdownParser", "Помилка читання: " + path, e);
        }
    }

    private void calculateContentHeight() {
        totalContentHeight = 0;

        List<MenuElement> activeElements = new ArrayList<>(globalElements);
        if (!tabs.isEmpty() && activeTabIndex < tabs.size()) {
            activeElements.addAll(tabs.get(activeTabIndex).elements);
        }

        for (MenuElement element : activeElements) {
            switch (element.type) {
                case H2: totalContentHeight += 65; break;
                case H4: totalContentHeight += 60; break;
                case H5: totalContentHeight += 55; break;
                case H6: totalContentHeight += 50; break;
                case PARAGRAPH:
                    font.getData().setScale(1.1f);
                    glyphLayout.setText(font, element.text, Color.WHITE, contentWidth, 8, true);
                    totalContentHeight += (glyphLayout.height + 25);
                    break;
                case IMAGE:
                    if (element.texture != null) {
                        totalContentHeight += element.texture.getHeight() + 30;
                    }
                    break;
            }
        }

        maxScrollY = Math.max(0, totalContentHeight - contentHeight);

        if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
        if (scrollOffsetY < 0) scrollOffsetY = 0;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollOffsetY += amountY * 45;
                if (scrollOffsetY < 0) scrollOffsetY = 0;
                if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
                return true;
            }
        });
    }

    private void handleInput(float wx, float wy) {
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scrollOffsetY -= 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scrollOffsetY += 300 * Gdx.graphics.getDeltaTime();
        }

        if (Gdx.input.justTouched()) {
            if (wx >= backBtnX && wx <= backBtnX + backBtnW && wy >= backBtnY && wy <= backBtnY + backBtnH) {
                switchMenu.switchMenu(MenuStatus.START_MENU);
                return;
            }

            // Розрахунок пропорційної ширини вкладок для обробки кліків
            float tabStartX = backBtnX + backBtnW + 30;
            float tabEndX = WORLD_W - 50;
            float availableWidth = tabEndX - tabStartX;
            float gap = 15;

            float tabW = tabs.isEmpty() ? 0 : (availableWidth - (tabs.size() - 1) * gap) / tabs.size();

            float currentTabX = tabStartX;
            for (int i = 0; i < tabs.size(); i++) {
                if (wx >= currentTabX && wx <= currentTabX + tabW && wy >= backBtnY && wy <= backBtnY + backBtnH) {
                    activeTabIndex = i;
                    scrollOffsetY = 0;
                    calculateContentHeight();
                    break;
                }
                currentTabX += tabW + gap;
            }

            if (maxScrollY > 0) {
                float scrollbarH = 60;
                float scrollThumbY = contentTop - scrollbarH - ((scrollOffsetY / maxScrollY) * (contentHeight - scrollbarH));
                if (wx >= scrollbarX && wx <= scrollbarX + scrollbarW && wy >= scrollThumbY && wy <= scrollThumbY + scrollbarH) {
                    isDraggingScrollbar = true;
                }
            }
        }

        if (!Gdx.input.isTouched()) {
            isDraggingScrollbar = false;
        } else if (isDraggingScrollbar && maxScrollY > 0) {
            float scrollbarH = 60;
            float relativeY = (contentTop - scrollbarH / 2f) - wy;
            float percentage = relativeY / (contentHeight - scrollbarH);
            scrollOffsetY = percentage * maxScrollY;
        }

        if (scrollOffsetY < 0) scrollOffsetY = 0;
        if (scrollOffsetY > maxScrollY) scrollOffsetY = maxScrollY;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.11f, 0.15f, 1f);

        com.badlogic.gdx.math.Vector3 touchPos = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPos);

        float wx = touchPos.x;
        float wy = touchPos.y;

        handleInput(wx, wy);

        if (game.getScreen() != this) return;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Розрахунок геометрії вкладок відповідно до максимальної ширини екрана
        float tabStartX = backBtnX + backBtnW + 30;
        float tabEndX = WORLD_W - 50;
        float availableWidth = tabEndX - tabStartX;
        float gap = 15;
        float tabW = tabs.isEmpty() ? 0 : (availableWidth - (tabs.size() - 1) * gap) / tabs.size();

        // =========================================================================
        // РЕНДЕРИНГ ЗАДНЬОГО ФОНУ КНОПОК
        // =========================================================================
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.FIREBRICK);
        shapeRenderer.rect(backBtnX, backBtnY, backBtnW, backBtnH);

        float currentTabX = tabStartX;
        for (int i = 0; i < tabs.size(); i++) {
            if (i == activeTabIndex) shapeRenderer.setColor(Color.DARK_GRAY);
            else shapeRenderer.setColor(Color.BLACK);

            shapeRenderer.rect(currentTabX, backBtnY, tabW, backBtnH);
            currentTabX += tabW + gap;
        }

        if (maxScrollY > 0) {
            float scrollbarH = 60;
            float scrollThumbY = contentTop - scrollbarH - ((scrollOffsetY / maxScrollY) * (contentHeight - scrollbarH));

            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(scrollbarX, contentBottom, scrollbarW, contentHeight);

            shapeRenderer.setColor(isDraggingScrollbar ? Color.GOLD : Color.LIGHT_GRAY);
            shapeRenderer.rect(scrollbarX, scrollThumbY, scrollbarW, scrollbarH);
        }
        shapeRenderer.end();

        // =========================================================================
        // РЕНДЕРИНГ ТЕКСТУ ВКЛАДОК (З АВТОПЕРЕНОСОМ)
        // =========================================================================
        batch.begin();
        if (h1Title != null && !h1Title.isEmpty()) {
            font.getData().setScale(2.3f);
            font.setColor(Color.GOLD);
            glyphLayout.setText(font, h1Title);
            font.draw(batch, h1Title, (WORLD_W - glyphLayout.width) / 2, WORLD_H - 40);
        }

        // Кнопка BACK
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        String back;
        if (language==LanguageUI.ENGLISH)
            back = "Back";
            else
            back = "Назад";

        glyphLayout.setText(font, back);
        font.draw(batch, back, backBtnX + (backBtnW - glyphLayout.width) / 2, backBtnY + (backBtnH + glyphLayout.height) / 2);

        // Малювання тексту вкладок з перевіркою на довжину і переносом рядків
        currentTabX = tabStartX;
        for (int i = 0; i < tabs.size(); i++) {
            font.getData().setScale(1.1f);
            font.setColor(i == activeTabIndex ? Color.GOLD : Color.LIGHT_GRAY);

            // Розраховуємо доступну ширину для тексту всередині вкладки за вирахуванням бокових відступів (по 10px з кожного боку)
            float targetWidth = tabW - 20;

            // Отримуємо висоту тексту із увімкненим переносом рядків (wrap = true)
            glyphLayout.setText(font, tabs.get(i).title, font.getColor(), targetWidth, Align.center, true);

            float textX = currentTabX + 10;
            // Універсальна формула динамічного вертикального центрування для одного або кількох рядків
            float textY = backBtnY + (backBtnH + glyphLayout.height) / 2f;

            // Малюємо текст із активованим параметром wrap
            font.draw(batch, tabs.get(i).title, textX, textY, targetWidth, Align.center, true);

            currentTabX += tabW + gap;
        }
        batch.end();

        // =========================================================================
        // ДИНАМІЧНИЙ SCISSOR TEST
        // =========================================================================
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        float screenScaleX = (float) viewport.getScreenWidth() / WORLD_W;
        float screenScaleY = (float) viewport.getScreenHeight() / WORLD_H;

        int scissorX = Math.round(viewport.getScreenX() + contentLeft * screenScaleX);
        int scissorY = Math.round(viewport.getScreenY() + contentBottom * screenScaleY);
        int scissorW = Math.round(contentWidth * screenScaleX);
        int scissorH = Math.round(contentHeight * screenScaleY);

        Gdx.gl.glScissor(scissorX, scissorY, scissorW, scissorH);

        batch.begin();
        float currentY = contentTop + scrollOffsetY;

        List<MenuElement> elementsToRender = new ArrayList<>(globalElements);
        if (!tabs.isEmpty() && activeTabIndex < tabs.size()) {
            elementsToRender.addAll(tabs.get(activeTabIndex).elements);
        }

        for (MenuElement element : elementsToRender) {
            switch (element.type) {
                case H2:
                    font.getData().setScale(1.7f);
                    font.setColor(Color.SKY);
                    font.draw(batch, element.text, contentLeft, currentY);
                    currentY -= 65;
                    break;
                case H4:
                    font.getData().setScale(1.4f);
                    font.setColor(Color.ORANGE);
                    font.draw(batch, element.text, contentLeft, currentY);
                    currentY -= 60;
                    break;
                case H5:
                    font.getData().setScale(1.2f);
                    font.setColor(Color.LIME);
                    font.draw(batch, element.text, contentLeft, currentY);
                    currentY -= 55;
                    break;
                case H6:
                    font.getData().setScale(1.1f);
                    font.setColor(Color.LIGHT_GRAY);
                    font.draw(batch, element.text, contentLeft, currentY);
                    currentY -= 50;
                    break;
                case PARAGRAPH:
                    font.getData().setScale(1.1f);
                    font.setColor(Color.WHITE);
                    font.draw(batch, element.text, contentLeft, currentY, contentWidth, 8, true);
                    glyphLayout.setText(font, element.text, Color.WHITE, contentWidth, 8, true);
                    currentY -= (glyphLayout.height + 25);
                    break;
                case IMAGE:
                    if (element.texture != null) {
                        float imgW = element.texture.getWidth();
                        float imgH = element.texture.getHeight();
                        float imgX = contentLeft + (contentWidth - imgW) / 2;
                        currentY -= imgH;
                        batch.draw(element.texture, imgX, currentY);
                        currentY -= 50;
                    }
                    break;
            }
        }
        batch.end();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        for (Texture texture : texturesToDispose) texture.dispose();
    }
}