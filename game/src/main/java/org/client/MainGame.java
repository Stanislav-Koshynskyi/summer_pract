package org.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.client.menu.*;
import org.core.enums.MenuStatus;

public class MainGame extends Game {
    public SpriteBatch batch;
    // public AssetLoader assetLoader; // закоментовано для прикладу, якщо класу ще немає

    private SwitchMenu switchMenu;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // assetLoader = new AssetLoader();
        // assetLoader.load();

        // Ініціалізуємо менеджер перемикання екранів
        switchMenu = new SwitchMenu(this);

        // РЕЄСТРАЦІЯ ЕКРАНІВ (Лямбда створює екран тільки в момент виклику)
        switchMenu.registerScreen(MenuStatus.START_MENU, () -> new StartMenu(this, switchMenu));

        // Коли ви створите інші класи меню, просто допишіть їх сюди без зміни SwitchMenu:
        // switchMenu.registerScreen(MenuStatus.SETTINGS_MENU, () -> new SettingsMenu(this, switchMenu));
        // switchMenu.registerScreen(MenuStatus.RULES_MENU, () -> new RulesMenu(this, switchMenu));

        switchMenu.registerScreen(MenuStatus.ABOUT_AUTHORS_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/aboutAuthours.md"));

        switchMenu.registerScreen(MenuStatus.SETTINGS_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/settings.md"));

        switchMenu.registerScreen(MenuStatus.RULES_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/rules.md"));

        // Початковий запуск стартового меню
        switchMenu.switchMenu(MenuStatus.START_MENU);
    }

    @Override
    public void dispose() {
        batch.dispose();
        // assetLoader.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}