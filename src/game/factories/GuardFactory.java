package game.factories;

import engine.CoreWorld;
import engine.Resources;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.rendering.Colour;
import game.ai.GuardState;
import game.components.*;

import java.util.List;

public class GuardFactory {
    CoreWorld world;
    Resources resources;
    String visionLayerResourceId;
    LayerID playerVision;

    public GuardFactory(CoreWorld world, Resources resources, String visionLayerResourceId, LayerID playerVision) {
        this.world = world;
        this.resources = resources;
        this.visionLayerResourceId = visionLayerResourceId;
        this.playerVision = playerVision;
    }

    public EntityID create(Point position, List<Point> path) {
        var guardVision = world.createLayer(
                new PositionComponent(new Point(0,0), 2),
                new VisibilityComponent(true),
                new DimensionsComponent(250,250),
                new MaskToTileMapComponent(playerVision)
        );

        var guard = world.createEntity(
                new PositionComponent(position, 3),
                new RenderableComponent('▶', null, null, true),
                new VisionEmitterComponent(150, 110, 5, guardVision, new Colour(0, 30, 30)),
                new OrientationComponent(12),
                new OrientToMovementComponent(),
                new MaskToTileMapComponent(playerVision),
                new VelocityComponent(1.2, 11,  "exponential"),
                new InventoryComponent(new InventoryComponent.Item("Staff Keycard", "A staff keycard with a basic access level")),
                new GuardComponent(path, GuardState.PATROLLING)
        );
        world.addComponentToLayer(guardVision, new VisionLayerComponent(guard));
        world.addComponentToLayer(guardVision, new TileMapComponent(visionLayerResourceId, guard.toString(), "tl", false));

        return guard;
    }
}
