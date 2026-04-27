package engine.systems.movement;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.MovementProcessor;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.LayerColliderComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.events.CollisionEvent;
import engine_interfaces.objects.events.LayerRemovedEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.events.MovementProposalEvent;
import engine_interfaces.objects.rendering.Cell;

import java.util.HashMap;
import java.util.HashSet;

import static engine.Utils.extractTilePointsFromTileMap;

public class CollisionProcessor implements MovementProcessor {
    // Stores baked collision data from non-moving layers.
    public HashSet<Point> staticCollisionMap = new HashSet<>();
    private final HashMap<LayerID, HashSet<Point>> layerCollisionPoints = new HashMap<>();
    private final EventBus bus;

    public CollisionProcessor(EventBus bus, World world, Resources resources) {
        this.bus = bus;

        bus.subscribe(LayerRegisteredEvent.class, () -> true,
            event -> {
                var layerRegisteredEvent = (LayerRegisteredEvent) event;
                var layerComponents = world.Layers.get(layerRegisteredEvent.id);
                if (layerComponents == null) {
                    return;
                }

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
                var cachedPointsForLayer = layerCollisionPoints.remove(layerRegisteredEvent.id);
                if (cachedPointsForLayer != null) {
                    staticCollisionMap.removeAll(cachedPointsForLayer);
                }

                var bakedPoints = extractTilePointsFromTileMap((TileMapComponent) tileMapComponent, dimensionsComponent, tileMapAsset, colliderDetails.collidableTiles, positionDetails);
                layerCollisionPoints.put(layerRegisteredEvent.id, bakedPoints);
                staticCollisionMap.addAll(bakedPoints);
            });

        bus.subscribe(LayerRemovedEvent.class, () -> true, event -> {
            var layerRemovedEvent = (LayerRemovedEvent) event;
            var cachedPointsForLayer = layerCollisionPoints.remove(layerRemovedEvent.id);
            if (cachedPointsForLayer != null) {
                staticCollisionMap.removeAll(cachedPointsForLayer);
            }
        });
    }

    @Override
    public boolean validateMove(HashMap<EntityID, HashMap<Class<? extends Component>, Component>> entityState, int tickCount, MovementProposalEvent proposal) {
        // only validates against static collision map for now


        boolean canMove = !staticCollisionMap.contains(proposal.proposedPosition);
        if (!canMove) {
            bus.publish(new CollisionEvent(proposal.entityID));
        }

        return canMove;
    }
}
