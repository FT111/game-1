package resources.scenes;

import engine.Engine;
import engine.scenes.Scene;
import resources.levels.level_1.Level1;
import resources.LevelContext;

public class GameplayScene extends Scene {

    public GameplayScene(Engine engine) {
        new Level1().run(new LevelContext(engine, this, world));
    }

    @Override
    protected void onEnter() {
    }
}
