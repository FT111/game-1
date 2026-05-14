package resources.scenes;

import engine.Engine;
import engine.World;
import engine.scenes.Scene;
import engine.systems.InputHandlerSystem;
import engine.systems.SceneGraphSystem;
import engine.systems.UiInteractionSystem;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import resources.CameraSystem;
import resources.ControlSystem;
import resources.MenuSystem;

public class GlobalScene extends Scene {
    public GlobalScene(Engine engine) {
        var camera = world.createEntity(
                new PositionComponent(new Point(0,0), 100),
                new CameraComponent(engine.Renderer.Api.getWidth(), engine.Renderer.Api.getHeight(), true)
        );

        add(new CameraSystem(camera, engine.Renderer.Api, engine.World));
        add(new UiInteractionSystem(engine.World, engine.EventBus, engine.Resources, engine.LayoutManager));
        add(new SceneGraphSystem(engine.EventBus));
        add(new MenuSystem(engine.EventBus, engine.World));
        add(new ControlSystem(engine.EventBus));
        add(new InputHandlerSystem(engine.Renderer.Api, engine.EventBus));


    }
}
