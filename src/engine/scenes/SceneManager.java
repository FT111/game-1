package engine.scenes;

import engine.EventBus;
import engine.Systems;
import engine.World;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import engine_interfaces.objects.events.SwitchSceneEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class SceneManager {
    private final Stack<Scene> sceneStack = new Stack<>();
    private final Map<String, Scene> scenes = new HashMap<>();
    private final World engineWorld;
    private final Systems systems;

    public SceneManager(EventBus bus, World world, Systems systems) {
        engineWorld = world;
        this.systems = systems;

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

    private Set<System> collectActiveSystems() {
        var activeSystems = new HashSet<System>();
        for (Scene scene : sceneStack) {
            activeSystems.addAll(scene.getSystems());
        }
        return activeSystems;
    }

    private void deactivateSystems(Set<System> previousActiveSystems, Set<System> targetActiveSystems) {
        for (System system : previousActiveSystems) {
            if (targetActiveSystems.contains(system)) {
                continue;
            }

            system.onExit(engineWorld);
            system.isEnabled = false;
        }
    }

    private void activateSystems(Set<System> previousActiveSystems, Set<System> targetActiveSystems) {
        for (System system : targetActiveSystems) {
            if (previousActiveSystems.contains(system)) {
                continue;
            }

            system.isEnabled = true;
            system.onEnter(engineWorld);
        }
    }

    private void notifyAfterSceneChange(Scene fromScene, Scene toScene) {
        this.systems.getSystems().forEach(system -> {
            system.afterSceneChange(fromScene, toScene, engineWorld);
        });
    }

    public void switchScene(String sceneName) {
        Scene newScene = scenes.get(sceneName);
        if (newScene == null) {
            throw new IllegalArgumentException("Scene " + sceneName + " not found");
        }

        if (sceneStack.size() == 1 && sceneStack.peek() == newScene) {
            return;
        }

        Set<System> previousActiveSystems = collectActiveSystems();
        Scene previousTopScene = sceneStack.isEmpty() ? null : sceneStack.peek();

        Set<System> targetActiveSystems = new HashSet<>(newScene.getSystems());
        deactivateSystems(previousActiveSystems, targetActiveSystems);

        while (!sceneStack.isEmpty()) {
            Scene activeScene = sceneStack.pop();
            activeScene.exit();
            engineWorld.remove(activeScene.world);
        }

        sceneStack.add(newScene);
        engineWorld.merge(newScene.world);
        newScene.enter();

        activateSystems(previousActiveSystems, targetActiveSystems);
        notifyAfterSceneChange(previousTopScene, newScene);
    }

    public void pushScene(String sceneName) {
        var scene = scenes.get(sceneName);
        if (scene == null) { throw new IllegalArgumentException("Scene " + sceneName + " not found"); }

        Set<System> previousActiveSystems = collectActiveSystems();
        Scene previousTopScene = sceneStack.isEmpty() ? null : sceneStack.peek();

        Set<System> targetActiveSystems = new HashSet<>(previousActiveSystems);
        targetActiveSystems.addAll(scene.getSystems());
        deactivateSystems(previousActiveSystems, targetActiveSystems);

        engineWorld.merge(scene.world);
        sceneStack.add(scene);
        scene.enter();

        activateSystems(previousActiveSystems, targetActiveSystems);
        notifyAfterSceneChange(previousTopScene, scene);
    }

    public void popScene() {
        if (sceneStack.isEmpty()) {
            throw new IllegalStateException("Cannot pop scene from an empty scene stack");
        }

        Set<System> previousActiveSystems = collectActiveSystems();
        var scene = sceneStack.pop();
        Scene newTopScene = sceneStack.isEmpty() ? null : sceneStack.peek();

        Set<System> targetActiveSystems = collectActiveSystems();
        deactivateSystems(previousActiveSystems, targetActiveSystems);

        scene.exit();
        engineWorld.remove(scene.world);

        activateSystems(previousActiveSystems, targetActiveSystems);
        notifyAfterSceneChange(scene, newTopScene);
    }

    public Stack<Scene> getSceneStack() {
        return sceneStack;
    }
}
