package resources.menus.states;

import engine.CoreWorld;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import resources.menus.KeyInputBind;
import resources.menus.MenuState;
import resources.menus.StateContext;
import resources.events.AwarenessChangedEvent;
import resources.events.GlobalAlertnessChangedEvent;
import resources.ai.GuardAlertness;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.rendering.Colour;

import java.util.HashMap;
import java.util.Map;

public class GameHud extends MenuState {
    private final Map<EntityID, Integer> awarenessMap = new HashMap<>();
    private final Map<EntityID, Integer> thresholdMap = new HashMap<>();
    private GuardAlertness globalAlertness = GuardAlertness.RELAXED;
    private final LayerID hudLayer;

    public GameHud(StateContext ctx) {
        super(ctx);

        // build HUD label (but don't make it visible here; MenuState.show will be
        // used by the MenuSystem to show/hide when the state becomes active)
        var ui = ctx.ui();
        var labelBuilder = ui.new LabelBuilder();
        // position at top-left (0,0) and raise z-index so HUD renders above other layers
        labelBuilder.withPosition(new Point(0, 0), Positioning.FIXED)
                .withZIndex(100)
                .withDimensions(40, 6)
                .withBorder('-', '|', '+', new Colour(255, 255, 255))
                .withStaticText("Awareness:\n");
        hudLayer = labelBuilder.build();
        show(hudLayer);

        // Subscribe to awareness changes; only update the HUD text when the
        // HUD layer is currently visible (MenuSystem controls visibility).
        ctx.bus().subscribe(AwarenessChangedEvent.class, () -> true, event -> {
            if (!(event instanceof AwarenessChangedEvent a)) return;
            awarenessMap.put(a.guardId, a.awareness);
            thresholdMap.put(a.guardId, a.threshold);
            updateHudText(ctx, hudLayer);
        });

        // Subscribe to global alertness changes so HUD displays the global state
        ctx.bus().subscribe(GlobalAlertnessChangedEvent.class, () -> true, event -> {
            if (!(event instanceof GlobalAlertnessChangedEvent g)) return;
            globalAlertness = g.level;
            updateHudText(ctx, hudLayer);
        });

        bindKeypress(new KeyInputBind('p'), () -> {
            ctx.switchTo().accept(ctx.states().mainMenu);
            ctx.bus().publish(new PopSceneEvent());
            ctx.bus().publish(new PushSceneEvent("MainMenu"));
        });
    }

    private void updateHudText(StateContext ctx, LayerID hudLayer) {
        var comps = ctx.elementComponents().apply(hudLayer);
        var textComp = CoreWorld.getComponent(comps, TextComponent.class);
        if (textComp == null) return;

        // compute max awareness
        EntityID maxGuard = null;
        int maxAw = 0;
        for (var e : awarenessMap.entrySet()) {
            if (e.getValue() > maxAw) {
                maxAw = e.getValue();
                maxGuard = e.getKey();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Awareness:\n");
        if (maxGuard == null) {
            sb.append("None\n");
            sb.append("Guard Alertness: ").append(globalAlertness.name()).append("\n");
            textComp.text = sb.toString();
            return;
        }

        int threshold = thresholdMap.getOrDefault(maxGuard, 1);
        int v = maxAw;
        int filled = (int) Math.round((v / (double) threshold) * 10);
        filled = Math.max(0, Math.min(10, filled));
        sb.append("[");
        for (int i = 0; i < filled; i++) sb.append('#');
        for (int i = filled; i < 10; i++) sb.append('-');
        sb.append("] ");
        sb.append(v).append("/").append(threshold).append("\n");
        sb.append("Guard Alertness: ").append(globalAlertness.name()).append("\n");

        textComp.text = sb.toString();
    }
}
