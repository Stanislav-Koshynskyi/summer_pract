package org.client.menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import org.client.MenuStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

public class SwitchMenu {
    private final Game game;
    // Реєстр фабрик для створення екранів
    private final Map<MenuStatus, Supplier<Screen>> screenRegistry = new HashMap<>();
    private final Stack<Screen> screenStack = new Stack<>();
    public SwitchMenu(Game game) {
        this.game = game;
    }

    /**
     * Реєструє спосіб створення екрану для певного статусу.
     */
    public void registerScreen(MenuStatus status, Supplier<Screen> screenSupplier) {
        screenRegistry.put(status, screenSupplier);
    }

    /**
     * Перемикає екран відповідно до статусу.
     */
    public void switchMenu(MenuStatus status) {
        Supplier<Screen> supplier = screenRegistry.get(status);

        if (supplier != null) {

            Screen currentScreen = game.getScreen();


            Screen nextScreen = supplier.get();
            game.setScreen(nextScreen);


            if (currentScreen != null) {
                currentScreen.dispose();
            }
        } else {
            Gdx.app.error("SwitchMenu", "Екран для статусу " + status + " не зареєстрований!");
        }
    }
    /**Для того щоб поставити гра на паузу*/
    public void pushScreen(MenuStatus status) {
        Screen currentScreen = game.getScreen();
        if (currentScreen != null) {
            currentScreen.pause();
            screenStack.push(currentScreen);
        }
        game.setScreen(screenRegistry.get(status).get());
    }
    /** відновити гру закрити вікно паузи*/
    public void popScreen() {
        Screen currentScreen = game.getScreen();
        if (!screenStack.isEmpty()) {
            Screen previousScreen = screenStack.pop();
            game.setScreen(previousScreen);
            previousScreen.resume();
        }
        if (currentScreen != null) currentScreen.dispose();
    }
    /**Очищення списку екранів*/
    public void clearStack(){
        screenStack.clear();
    }
}