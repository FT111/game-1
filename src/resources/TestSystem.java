package resources;

import engine.World;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;

public class TestSystem extends System {
    LayerID level;

    public TestSystem(LayerID level) {
        super();
        this.level = level;
    }


    @Override
    public void update(World world) {
        PositionComponent levelPos = (PositionComponent) world.Layers.get(level).get(PositionComponent.class);
        levelPos.Origin = new Point(levelPos.Origin.x(), levelPos.Origin.y()+1);

    }
}
