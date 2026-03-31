package engine.systems.movement;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.MovementProcessor;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.LayerColliderComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.events.CollisionEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.events.MovementProposalEvent;
import engine_interfaces.objects.rendering.Cell;

import java.util.HashMap;
import java.util.HashSet;

import static engine.Utils.extractTilePointsFromTileMap;

public class CollisionProcessor implements MovementProcessor {
    // Stores baked collision data from non-moving layers.
    public HashSet<Point> staticCollisionMap = new HashSet<>();
    private final EventBus Bus;

    public CollisionProcessor(EventBus Bus, World world, Resources resources) {
        this.Bus = Bus;

        Bus.subscribe(LayerRegisteredEvent.class, "CollisionSystem",
            event -> {
                var layerRegisteredEvent = (LayerRegisteredEvent) event;
                var layerComponents = world.Layers.get(layerRegisteredEvent.id);
                var colliderComponent = layerComponents.getOrDefault(LayerColliderComponent.class, null);
                var positionComponent = layerComponents.getOrDefault(PositionComponent.class, null);
                var tileMapComponent = layerComponents.getOrDefault(TileMapComponent.class, null);
                var dimensionsComponent = (DimensionsComponent) layerComponents.getOrDefault(DimensionsComponent.class, null);

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
                staticCollisionMap.addAll(extractTilePointsFromTileMap((TileMapComponent) tileMapComponent, dimensionsComponent, tileMapAsset, colliderDetails.collidableTiles, positionDetails));
            });
    }

    @Override
    public boolean validateMove(HashMap<EntityID, HashMap<Class<? extends Component>, Component>> entityState, int tickCount, MovementProposalEvent proposal) {
        // only validates against static collision map for now


        boolean canMove = !staticCollisionMap.contains(proposal.proposedPosition);
        if (!canMove) {
            Bus.publish(new CollisionEvent(proposal.entityID));
        }

        return canMove;
    }
}
