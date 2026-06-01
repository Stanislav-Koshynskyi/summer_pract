package org.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class CoreGame extends ApplicationAdapter {
    @Override
    public void create() {
        // Load resources into memory
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        // Placeholder
    }

    private void logic() {
        // Placeholder
    }

    private void draw() {
        ScreenUtils.clear(Color.BLUE);
    }
}
