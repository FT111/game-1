package game.scenes;

import engine.Engine;
import engine.scenes.Scene;
import game.levels.level_1.Level1;
import game.LevelContext;

public class GameplayScene extends Scene {

    public GameplayScene(Engine engine) {
        new Level1().run(new LevelContext(engine, this, world));
    }

    @Override
    protected void onEnter() {
    }
}
