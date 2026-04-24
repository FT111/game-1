package engine.scenes;

import engine.EventBus;
import engine.Systems;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import engine_interfaces.objects.events.SwitchSceneEvent;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class SceneManager {
    private Stack<Scene> sceneStack = new Stack<>();
    private final Map<String, Scene> scenes = new HashMap<>();
    private final World engineWorld;

    public SceneManager(EventBus bus, World world) {
        engineWorld = world;

        bus.subscribe(SwitchSceneEvent.class, ()-> true, event -> {
            var eventData = (SwitchSceneEvent) event;
            switchScene(eventData.sceneName);
        });

        bus.subscribe(PushSceneEvent.class, ()-> true, event -> {
            var eventData = (PushSceneEvent) event;
            pushScene(eventData.sceneName);
        });

        bus.subscribe(PopSceneEvent.class, ()-> true, event -> {
            popScene();
        });
    }

    public SceneManager addScene(String name, Scene scene) {
        scenes.put(name, scene);
        return this;
    }

    public void switchScene(String sceneName) {
        Scene newScene = scenes.get(sceneName);
        if (newScene == null) {
            throw new IllegalArgumentException("Scene " + sceneName + " not found");
        }


        while (!sceneStack.isEmpty()) {
            Scene activeScene = sceneStack.pop();
            // Disable systems that are not in the new scene

            engineWorld.remove(activeScene.world);

            Set<System> removedSystems = activeScene.getSystems().stream()
                .filter(system -> !newScene.getSystems().contains(system))
                .collect(Collectors.toSet());

            for (System sys : removedSystems) {
                sys.isEnabled = false;
            }

            activeScene.exit();
        }

        sceneStack.add(newScene);
        // Enable new systems
        for (System sys : newScene.getSystems()) {
            sys.isEnabled = true;
        }
        engineWorld.merge(newScene.world);
    }

    public void pushScene(String sceneName) {
        var scene = scenes.get(sceneName);
        if (scene == null) { throw new IllegalArgumentException("Scene " + sceneName + " not found"); }

        for (System sys : scene.getSystems()) {
            sys.isEnabled = true;
        }
        engineWorld.merge(scene.world);
        sceneStack.add(scene);
    }

    public void popScene() {
        var scene = sceneStack.pop();

        engineWorld.remove(scene.world);
        for (System system : scene.getSystems()) {
            system.isEnabled = false;
        }
    }

    public Stack<Scene> getSceneStack() {
        return sceneStack;
    }
}
