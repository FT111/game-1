package resources;

import engine.World;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.components.ui.ButtonComponent;
import engine_interfaces.objects.ui.SelectionStrategies;

public class UiBuilders {
    private World world;

    public UiBuilders(World world) {
        this.world = world;
    }

    public abstract class Builder<B extends Builder<B, T>, T> {
        Point position;
        Positioning positioningStrategy = Positioning.ABSOLUTE;
        LayerID parent;
        int zIndex = 0;

        protected T object;

        protected abstract B self();

        protected abstract T build();


        public B withPosition(Point position) {
            this.position = position;
            return self();
        }

        public B withPosition(Point position, Positioning positionStrategy) {
            this.position = position;
            this.positioningStrategy = positionStrategy;
            return self();
        }

        public B withZIndex(int zIndex) {
            this.zIndex = zIndex;
            return self();
        }
    }

    public class ButtonBuilder extends Builder<ButtonBuilder, LayerID> {
        private String staticTextString;

        private String textResouceId;
        private String textAssetId;

        private int height;
        private int width;

        @Override
        public ButtonBuilder self() {
            return this;
        }

        public ButtonBuilder withStaticText(String text) {
            this.staticTextString = text;
            return this;
        }

        public ButtonBuilder withDynamicText(String resourceId, String assetId) {
            this.textResouceId = resourceId;
            this.textAssetId = assetId;
            return this;
        }

        public ButtonBuilder withDimensions(int width, int height) {
            this.width = width;
            this.height = height;

            return this;
        }

        @Override
        public LayerID build() {
            LayerID buttonLayer = world.createLayer(
                    new VisibilityComponent(true),
                    new PositionComponent(position, zIndex, positioningStrategy),
                    new ButtonComponent(SelectionStrategies.BOUNDING),
                    new DimensionsComponent(width, height)
            );
            if (staticTextString != null) {
                world.Layers.get(buttonLayer).put(TextComponent.class, new TextComponent(staticTextString, width, height));
            }
            if (textResouceId != null && textAssetId != null) {
                world.Layers.get(buttonLayer).put(TextComponent.class, new TextComponent(textResouceId, textAssetId, height, width));
            }

            return buttonLayer;
        }
    }

    // Separated to show clearer intent
    public class LabelBuilder extends ButtonBuilder {}
}
