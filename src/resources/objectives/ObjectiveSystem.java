package resources.objectives;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.rendering.Colour;
import resources.UiBuilders;

public class ObjectiveSystem extends System {
    private final ObjectiveManager manager;
    private final EventBus bus;
    private LayerID hudLayer;

    public ObjectiveSystem(ObjectiveManager manager, EventBus bus) {
        this.manager = manager;
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        super.onEnter(world);
        UiBuilders ui = new UiBuilders(world);
        var labelBuilder = ui.new LabelBuilder<>();
        // Position it on top right (or dynamically near right edge)
        labelBuilder.withPosition(new Point(0, 0), Positioning.FIXED)
                .withZIndex(100)
                .withDimensions(40, 30) // increased height, width slightly
                .withAlignment(Alignment.TOP_RIGHT)
                .withBorder('-', '|', '+', new Colour(255, 255, 255))
                .withStaticText("");
        hudLayer = labelBuilder.build();
        world.Layers.get(hudLayer).put(VisibilityComponent.class, new VisibilityComponent(true));
    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);

        // update objective states
        manager.update(world, bus);

        // update ui Text
        if (hudLayer != null) {
            var comps = world.Layers.get(hudLayer);
            var textComp = (TextComponent) comps.get(TextComponent.class);
            if (textComp != null) {
                StringBuilder sb = new StringBuilder();
                for (Objective obj : manager.getObjectives()) {
                    sb.append(obj.getName()).append(obj.isCompleted() ? " (COMPLETED)\n" : "\n");
                    for (ObjectiveRule rule : obj.getRules()) {
                        boolean met = rule.isMet(world, bus);
                        sb.append(met ? " [x] " : " [ ] ")
                          .append(rule.getDescription())
                          .append("\n");
                    }
                    sb.append("\n");
                }
                textComp.text = sb.toString();
            }
        }
    }
}
