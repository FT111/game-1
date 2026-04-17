package resources;

import engine.World;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.components.ui.ButtonComponent;
import engine_interfaces.objects.rendering.PositioningCalculators;
import engine_interfaces.objects.ui.SelectionStrategies;

import java.util.List;

public class UiBuilders {
    private World world;

    public UiBuilders(World world) {
        this.world = world;
    }

    public class ButtonBuilder {
        private String text;
        private Point position;
        private Positioning positioningStrategy = Positioning.ABSOLUTE;
        private LayerID parent;
        private int zIndex = 0;

        public ButtonBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public ButtonBuilder withPosition(Point position) {
            this.position = position;
            return this;
        }

        public ButtonBuilder withPosition(Point position, Positioning positionStrategy) {
            this.position = position;
            this.positioningStrategy = positionStrategy;
            return this;
        }

        public ButtonBuilder withZIndex(int zIndex) {
            this.zIndex = zIndex;
            return this;
        }

        public LayerID build() {
            LayerID buttonLayer = world.createLayer(
                    new VisibilityComponent(true),
                    new PositionComponent(position, zIndex, positioningStrategy),
                    new ButtonComponent(SelectionStrategies.BOUNDING)
            );
            if (text != null) { world.Layers.get(buttonLayer).put(TextComponent.class, new TextComponent(text)); }

            return buttonLayer;
        }
    }
}
