package resources;

import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.components.ui.ClickComponent;
import engine_interfaces.objects.components.ui.HoverComponent;
import engine_interfaces.objects.rendering.Colour;
import engine_interfaces.objects.ui.SelectionStrategies;
import resources.menus.InteractionHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UiBuilders {
    private InteractionHooks hooks;
    private final World world;

    public UiBuilders(World world) {
        this.world = world;
    }

    public UiBuilders(World world, InteractionHooks hooks) {
        this.world = world;
        this.hooks = hooks;
    }

    public void setInteractionHooks(InteractionHooks hooks) {
        this.hooks = hooks;
    }

    public abstract class Builder<B extends Builder<B, T>, T> {
        Point position;
        Positioning positioningStrategy = Positioning.ABSOLUTE;
        LayerID parent;
        int zIndex = 0;
        Alignment alignment;

        BackgroundComponent backgroundComponent;
        BorderComponent borderComponent;
        Consumer<Map<Class<? extends Component>, Component>> hoverEnterCallback;
        Consumer<Map<Class<? extends Component>, Component>> hoverExitCallback;

        protected T object;

        protected abstract B self();

        public abstract T build();


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

        public B onHoverEnter(Consumer<Map<Class<? extends Component>, Component>> callback) {
            this.hoverEnterCallback = callback;
            return self();
        }

        public B onHoverExit(Consumer<Map<Class<? extends Component>, Component>> callback) {
            this.hoverExitCallback = callback;
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

        protected void applyHoverComponents(LayerID layer) {
            if (hoverExitCallback != null) {
                hooks.bindHoverExit().apply(layer, hoverExitCallback);
            }
            if (hoverEnterCallback != null) {
                hooks.bindHoverEnter().apply(layer, hoverEnterCallback);
            }
        }
    }

    protected void applyTextDimensionalComponents(LayerID layer, boolean autoSizing, int width, int height) {

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

            return self();
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
            applyHoverComponents(containerLayer);

            return containerLayer;
        }
    }

    public class LabelBuilder<B extends LabelBuilder<B>> extends Builder<B, LayerID> {
        protected String staticTextString;

        protected String textResourceId;
        protected String textAssetId;

        protected int height;
        protected int width;

        @Override
        @SuppressWarnings("unchecked")
        public B self() {
            return (B) this;
        }

        public B withStaticText(String text) {
            this.staticTextString = text;
            return self();
        }

        public B withDynamicText(String resourceId, String assetId) {
            this.textResourceId = resourceId;
            this.textAssetId = assetId;
            return self();
        }

        public B withDimensions(int width, int height) {
            this.width = width;
            this.height = height;

            return self();
        }

        public B withAutoSizing() {

            return self();
        }

        @Override
        public LayerID build() {
            LayerID buttonLayer = world.createLayer(
                    new VisibilityComponent(false),
                    new PositionComponent(position, zIndex, positioningStrategy, alignment),
                    new HoverComponent(SelectionStrategies.BOUNDING),
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
            applyHoverComponents(buttonLayer);


            return buttonLayer;
        }
    }

    public class ButtonBuilder extends LabelBuilder<ButtonBuilder> {

        private Consumer<Map<Class<? extends Component>, Component>> clickCallback;

        @Override
        public ButtonBuilder self() {
            return this;
        }

        public ButtonBuilder onClick(Consumer<Map<Class<? extends Component>, Component>> callback) {
            this.clickCallback = callback;
            return self();
        }

        @Override
        public LayerID build() {
            LayerID buttonLayer = world.createLayer(
                    new VisibilityComponent(false),
                    new PositionComponent(position, zIndex, positioningStrategy, alignment),
                    new ClickComponent(SelectionStrategies.BOUNDING),
                    new HoverComponent(SelectionStrategies.BOUNDING),
                    new DimensionsComponent(width, height)
            );
            if (staticTextString != null) {
                world.addComponentToLayer(buttonLayer, new TextComponent(staticTextString, width, height));
            }
            if (textResourceId != null && textAssetId != null) {
                world.addComponentToLayer(buttonLayer, new TextComponent(textResourceId, textAssetId, height, width));
            }
            if (parent != null) { world.addComponentToLayer(buttonLayer, new ParentComponent(parent));}
            if (clickCallback != null) {
                hooks.bindClick().apply(buttonLayer, clickCallback);
            }
            applyBlockComponents(buttonLayer);
            applyHoverComponents(buttonLayer);

            return buttonLayer;
        }
    }
}
