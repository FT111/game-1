package engine.systems;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.ParentComponent;
import engine_interfaces.objects.events.EntityRegisteredEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.scene.SceneNode;

import java.util.NoSuchElementException;

public class SceneGraphSystem extends System {

    public SceneGraphSystem(World world, EventBus bus) {
        bus.subscribe(LayerRegisteredEvent.class, "SceneGraphSystem", (event -> handleLayerRegistered((LayerRegisteredEvent) event, world)));
//        bus.subscribe(EntityRegisteredEvent.class, "SceneGraphSystem", (event -> handleEntityRegistered((EntityRegisteredEvent) event, world)));
    }

    private void handleLayerRegistered(LayerRegisteredEvent event, World world) {
        var layer = world.Layers.get(event.id);
        var layerParent = (ParentComponent) layer.get(ParentComponent.class);

        registerToScene(event.id, world, layerParent);
    }

    private static void registerToScene(LayerID id, World world, ParentComponent layerParent) {
        var node = new SceneNode<>(id);

        if (layerParent.parentLayerId != null) {
            var parentNode = world.layerSceneGraphRoot.findNode(layerParent.parentLayerId);
            if (parentNode == null) { throw new NoSuchElementException("Parent node not registered");}

            world.layerSceneGraphNodes.put(id, node);
            parentNode.attach(node);
            return;
        }

        world.layerSceneGraphNodes.put(id, node);
        world.layerSceneGraphRoot.attach(node);
    }

    @Override
    public void update(World world, int tickCount) {

    }
}
