package resources;

import engine.World;
import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.components.ui.ClickComponent;
import engine_interfaces.objects.rendering.Colour;
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
        Alignment alignment;

        BackgroundComponent backgroundComponent;
        BorderComponent borderComponent;

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

        public B withParent(LayerID layerId) {
            this.parent = layerId;
            return self();
        }

        public B withAlignment(Alignment alignment) {
            this.alignment = alignment;
            return self();
        }

        public B withZIndex(int zIndex) {
            this.zIndex = zIndex;
            return self();
        }

        public B withBackground(Colour bgColour) {
            this.backgroundComponent = new BackgroundComponent(bgColour);
            return self();
        }

        public B withBackground(Colour bgColour, Character fillChar) {
            this.backgroundComponent = new BackgroundComponent(bgColour, fillChar, true);
            return self();
        }

        public B withBackground(Colour bgColour, Character fillChar, int zIndex) {
            this.backgroundComponent = new BackgroundComponent(bgColour, fillChar, true, zIndex);
            return self();
        }

        public B withBorder(Character horizontalChar, Character verticalChar, Character cornerChar, Colour fgColour) {
            this.borderComponent = new BorderComponent(horizontalChar, verticalChar, cornerChar, fgColour);
            return self();
        }

        public B withBorder(Character horizontalChar, Character verticalChar, Character cornerChar, Colour fgColour, int thickness) {
            this.borderComponent = new BorderComponent(horizontalChar, verticalChar, cornerChar, fgColour, true, thickness);
            return self();
        }

        protected void applyBlockComponents(LayerID layer) {
            if (backgroundComponent != null) {
                world.addComponentToLayer(layer, backgroundComponent);
            }
            if (borderComponent != null) {
                world.addComponentToLayer(layer, borderComponent);
            }
        }
    }

    public class ContainerBuilder extends Builder<ContainerBuilder, LayerID> {
        private int height;
        private int width;

        @Override
        public ContainerBuilder self() {
            return this;
        }

        public ContainerBuilder withDimensions(int width, int height) {
            this.width = width;
            this.height = height;

            return this;
        }

        @Override
        public LayerID build() {
            LayerID containerLayer = world.createLayer(
                    new VisibilityComponent(false),
                    new PositionComponent(position, zIndex, positioningStrategy, alignment),
                    new DimensionsComponent(width, height)
            );
            if (parent != null) { world.addComponentToLayer(containerLayer, new ParentComponent(parent));}
            applyBlockComponents(containerLayer);

            return containerLayer;
        }
    }

    public class ButtonBuilder extends Builder<ButtonBuilder, LayerID> {
        private String staticTextString;

        private String textResourceId;
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
            this.textResourceId = resourceId;
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
                    new VisibilityComponent(false),
                    new PositionComponent(position, zIndex, positioningStrategy, alignment),
                    new ClickComponent(SelectionStrategies.BOUNDING),
                    new DimensionsComponent(width, height)
            );
            if (staticTextString != null) {
                world.addComponentToLayer(buttonLayer, new TextComponent(staticTextString, width, height));
            }
            if (textResourceId != null && textAssetId != null) {
                world.addComponentToLayer(buttonLayer, new TextComponent(textResourceId, textAssetId, height, width));
            }
            if (parent != null) { world.addComponentToLayer(buttonLayer, new ParentComponent(parent));}
            applyBlockComponents(buttonLayer);


            return buttonLayer;
        }
    }

    // Separated to show clearer intent
    public class LabelBuilder extends ButtonBuilder {}
}
