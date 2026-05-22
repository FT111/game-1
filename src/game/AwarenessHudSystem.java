package game;

import engine.CoreWorld;
import engine.EventBus;
import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.rendering.Colour;
import game.events.AwarenessChangedEvent;
import game.events.GlobalAlertnessChangedEvent;
import game.ai.GuardAlertness;
import engine_interfaces.objects.events.NewGameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwarenessHudSystem extends System {
    private final EventBus bus;
    private final World world;
    private LayerID hudLayer;
    private final Map<EntityID, Integer> awarenessMap = new HashMap<>();
    private GuardAlertness globalAlertness = GuardAlertness.RELAXED;
    // track threshold per guard so we can report the correct denominator for the
    // guard that currently has the highest awareness
    private final List<EventSubscriptionReceipt> subscriptions = new ArrayList<>();

    public AwarenessHudSystem(EventBus bus, World world) {
        this.bus = bus;
        this.world = world;
    }

    @Override
    public void onEnter(World world) {
        super.onEnter(world);
        // Build a simple HUD label in the top-left of the screen
        UiBuilders ui = new UiBuilders(world);
        var labelBuilder = ui.new LabelBuilder();
        labelBuilder.withPosition(new Point(0, 0), Positioning.FIXED)
                .withDimensions(40, 6)
                .withZIndex(100)
                .withBorder('-', '|', '+', new Colour(255, 255, 255))
                .withStaticText("Awareness:\n");
        hudLayer = labelBuilder.build();
        // make visible
        world.Layers.get(hudLayer).put(VisibilityComponent.class, new VisibilityComponent(true));

        subscriptions.add(bus.subscribe(AwarenessChangedEvent.class, () -> isEnabled, this::handleAwarenessChanged));
        subscriptions.add(bus.subscribe(GlobalAlertnessChangedEvent.class, () -> isEnabled, this::handleGlobalAlertnessChanged));
        subscriptions.add(bus.subscribe(NewGameEvent.class, () -> isEnabled, e -> {
            awarenessMap.clear();
            globalAlertness = GuardAlertness.RELAXED;
            updateHudText(1);
        }));
    }

    @Override
    public void onExit(World world) {
        super.onExit(world);
        subscriptions.forEach(s -> s.cancel.run());
        subscriptions.clear();
    }

    private void handleAwarenessChanged(Event event) {
        if (!(event instanceof AwarenessChangedEvent a)) return;
        awarenessMap.put(a.guardId, a.awareness);
        updateHudText(a.threshold);
    }

    private void handleGlobalAlertnessChanged(Event event) {
        if (!(event instanceof GlobalAlertnessChangedEvent g)) return;
        globalAlertness = g.level;
        // refresh HUD; use 1 as a safe threshold if none is available
        updateHudText(1);
    }

    private void updateHudText(int threshold) {
        if (hudLayer == null) return;
        var comps = world.Layers.get(hudLayer);
        var textComp = CoreWorld.getComponent(comps, TextComponent.class);
        if (textComp == null) return;
        // compute the guard with maximum awareness
        EntityID maxGuard = null;
        int maxAw = 0;
        for (var e : awarenessMap.entrySet()) {
            if (e.getValue() > maxAw) {
                maxAw = e.getValue();
                maxGuard = e.getKey();
            }
        }
        int filled;

        StringBuilder sb = new StringBuilder();
        sb.append("Awareness:\n");
        if (maxGuard == null) {
            filled = 0;
        }
        else {
            // render single bar of up to 10 characters proportional to threshold
            filled = (int) Math.round((maxAw / (double) threshold) * 25);
            filled = Math.max(0, Math.min(25, filled));
        }


        sb.append("[");
        for (int i = 0; i < filled; i++) sb.append('#');
        for (int i = filled; i < 25; i++) sb.append('-');
        sb.append("] ");
        sb.append(maxAw).append("/").append(threshold).append("\n");
        sb.append("Guard Alertness: ").append(globalAlertness.name()).append("\n");

        textComp.text = sb.toString();
    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);
    }
}
