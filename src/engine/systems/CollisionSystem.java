package engine.systems;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.LayerColliderComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.events.CollisionEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.events.MovementProposalEvent;
import engine_interfaces.objects.rendering.Cell;

import javax.sound.midi.MidiFileFormat;
import java.util.HashMap;
import java.util.HashSet;

public class CollisionSystem extends System {
    // Stores baked collision data from non-moving layers.
    public HashSet<Point> staticCollisionMap = new HashSet<>();

    public CollisionSystem(EventBus Bus, World world, Resources resources) {
        Bus.subscribe(LayerRegisteredEvent.class, "CollisionSystem",
            event -> {
                var layerRegisteredEvent = (LayerRegisteredEvent) event;
                var layerComponents = world.Layers.get(layerRegisteredEvent.id);
                var colliderComponent = layerComponents.getOrDefault(LayerColliderComponent.class, null);
                var positionComponent = layerComponents.getOrDefault(PositionComponent.class, null);
                var tileMapComponent = layerComponents.getOrDefault(TileMapComponent.class, null);

                // check all exist
                if (colliderComponent == null || positionComponent == null || tileMapComponent == null) {
                    return;
                }
                // bake into global collision map
                var colliderDetails = (LayerColliderComponent) colliderComponent;
                var positionDetails = (PositionComponent) positionComponent;
                var tileMapDetails = (TileMapComponent) tileMapComponent;

                var tileMapAsset = resources.getAsset(tileMapDetails.resourceId, tileMapDetails.assetId, Cell[][].class);

                // find collidable cells
                for (int y = 0; y < tileMapAsset.length; y++) {
                    for (int x = 0; x < tileMapAsset[0].length; x++) {
                        Cell cell = tileMapAsset[y][x];
                        if (cell == null) {
                            continue;
                        }
                        if (colliderDetails.collidableTiles.contains(cell.content)) {
                            Point collisionPoint = new Point(positionDetails.Origin.x() + x, positionDetails.Origin.y() + y);
                            staticCollisionMap.add(collisionPoint);
                        }
                    }
                }
            });

        Bus.subscribe(MovementProposalEvent.class, "CollisionSystem",
            event -> {
            var proposedMovement = (MovementProposalEvent) event;
                MovementProposalEvent movementDetails = (MovementProposalEvent) event;
                if (staticCollisionMap.contains(movementDetails.proposedPosition)) {
                    Bus.publish(new CollisionEvent(movementDetails.entityID));
                }
            });
    }

    @Override
    public void update(World world, int tickCount) {

    }
}
