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
import engine.layout.LayoutManager;

import java.util.HashMap;
import java.util.HashSet;

import static engine.Utils.extractTilePointsFromTileMap;

public class CollisionProcessor implements MovementProcessor {
    private final EventBus bus;
    private final LayoutManager layoutManager;

    public CollisionProcessor(EventBus bus, World world, Resources resources, LayoutManager layoutManager) {
        this.bus = bus;
        this.layoutManager = layoutManager;
    }

    @Override
    public boolean validateMove(HashMap<EntityID, HashMap<Class<? extends Component>, Component>> entityState, int tickCount, MovementProposalEvent proposal) {
        // Skip collision checks for the camera
        HashMap<Class<? extends Component>, Component> components = entityState.get(proposal.entityID);
        if (components != null && components.containsKey(engine_interfaces.objects.components.CameraComponent.class)) {
            return true;
        }

        // only validates against static collision map for now
        var collisionMap = layoutManager.getSpatialMap("collision");

        boolean canMove = collisionMap.getLayersAt(proposal.proposedPosition).isEmpty();
        if (!canMove) {
            bus.publish(new CollisionEvent(proposal.entityID));
        }

        return canMove;
    }
}
