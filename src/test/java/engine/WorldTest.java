package engine;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.LayerRemovedEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldTest {

    @Test
    void createEntityAddsToComponentIndex() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        EntityID id = world.createEntity(new PositionComponent(new engine_interfaces.objects.Point(0, 0)));

        assertTrue(world.ComponentEntitiesIndex.query(PositionComponent.class).contains(id));
    }

    @Test
    void addComponentToLayerUpdatesIndex() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        LayerID layerId = world.createLayer();
        world.addComponentToLayer(layerId, new DimensionsComponent(2, 3));

        assertTrue(world.ComponentLayersIndex.query(DimensionsComponent.class).contains(layerId));
    }

    @Test
    void removeLayerPublishesEvent() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        AtomicInteger calls = new AtomicInteger(0);
        bus.subscribe(LayerRemovedEvent.class, () -> true, event -> calls.incrementAndGet());

        LayerID layerId = world.createLayer();
        world.removeLayer(layerId);
        bus.flush();

        assertEquals(1, calls.get());
    }
}
