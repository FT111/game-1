package game;

import engine.Engine;
import engine.scenes.Scene;
import engine.scenes.SceneWorld;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;

import java.util.HashMap;
import java.util.HashSet;

public class LevelContext {
    public final Engine engine;
    public final Scene scene;
    public final SceneWorld world;
    public final HashMap<Point, HashSet<EntityID>> chunkMap = new HashMap<>();
    public LayerID playerVision;

    public LevelContext(Engine engine, Scene scene, SceneWorld world) {
        this.engine = engine;
        this.scene = scene;
        this.world = world;
    }
}
