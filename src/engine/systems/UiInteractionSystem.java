package engine.systems;

import engine.EventBus;
import engine.Logs;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.components.ui.ClickComponent;
import engine_interfaces.objects.components.ui.HoverComponent;
import engine_interfaces.objects.events.ButtonClickEvent;
import engine_interfaces.objects.events.LayerHoverEvent;
import engine_interfaces.objects.events.LayerHoverExitEvent;
import engine_interfaces.objects.events.LayerRemovedEvent;
import engine_interfaces.objects.events.MouseInputEvent;
import engine_interfaces.objects.rendering.Cell;
import engine.layout.LayoutManager;
import engine_interfaces.objects.ui.IndexedInteractable;
import engine_interfaces.objects.ui.InteractionApprovals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/// Responsible for handling UI interactions, including click and hover events.
/// Subscribes to mouse events and emits the appropriate UI interaction event
public class UiInteractionSystem extends System {
    private IndexedInteractable focusedLayer = null;
    private LayerID lastHoveredLayer = null;
    private final World world;
    private final EventBus bus;
    private final Resources resources;
    private final LayoutManager layoutManager;

    private EventSubscriptionReceipt mouseInputSubscription;
    private EventSubscriptionReceipt layerRemovedSubscription;

    public UiInteractionSystem(World world, EventBus bus, Resources resources, LayoutManager layoutManager) {
        this.world = world;
        this.bus = bus;
        this.resources = resources;
        this.layoutManager = layoutManager;
    }

    @Override
    public void onEnter(World world) {
        mouseInputSubscription = bus.subscribe(MouseInputEvent.class, () -> isEnabled, event -> {
            var input = (MouseInputEvent) event;
            if (input.eventType != MouseEventTypes.DOWN && input.eventType != MouseEventTypes.MOVE) {
                return;
            }

            var cameraView = layoutManager.getActiveCameraView();
            if (cameraView == null) {
                if (input.eventType == MouseEventTypes.DOWN) {
                    focusedLayer = null;
                } else if (input.eventType == MouseEventTypes.MOVE && lastHoveredLayer != null) {
                    bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
                    lastHoveredLayer = null;
                }
                return;
            }

            var target = findFirstInteractable(input, cameraView);
            if (target == null) {
                if (input.eventType == MouseEventTypes.DOWN) {
                    focusedLayer = null;
                } else if (input.eventType == MouseEventTypes.MOVE && lastHoveredLayer != null) {
                    bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
                    lastHoveredLayer = null;
                }
                return;
            }

            emitEventOnInteraction(input, target);
        });

        layerRemovedSubscription = bus.subscribe(LayerRemovedEvent.class, () -> isEnabled, event -> {
            var layerRemovedEvent = (LayerRemovedEvent) event;
            if (lastHoveredLayer != null && lastHoveredLayer.equals(layerRemovedEvent.id)) {
                bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
                lastHoveredLayer = null;
            }

            if (focusedLayer != null && focusedLayer.layerID().equals(layerRemovedEvent.id)) {
                focusedLayer = null;
            }
        });

        focusedLayer = null;
        lastHoveredLayer = null;
    }

    private static InteractionApprovals checkIfInteractable(World world, IndexedInteractable screenElement) {
        HashMap<Class<? extends Component>, Component> element = world.Layers.get(screenElement.layerID());
        if (element == null) {
            return new InteractionApprovals(false, false);
        }

        var visibility = (VisibilityComponent) element.get(VisibilityComponent.class);
        var hover = (HoverComponent) element.get(HoverComponent.class);
        var click = (ClickComponent) element.get(ClickComponent.class);

        return new InteractionApprovals(
                click != null && (!click.visibilityDependent || visibility != null && visibility.isVisible),
                hover != null && (!hover.visibilityDependent || visibility != null && visibility.isVisible)
        );

    }

    private void emitEventOnInteraction(MouseInputEvent input, IndexedInteractable screenElement) {
        InteractionApprovals approvals = checkIfInteractable(world, screenElement);

        if (input.eventType == MouseEventTypes.DOWN && approvals.isClickable()) {
            focusedLayer = screenElement;
            bus.publish(new ButtonClickEvent(screenElement.layerID()));
        } else if (input.eventType == MouseEventTypes.MOVE) {
            if (lastHoveredLayer != null && !lastHoveredLayer.equals(screenElement.layerID())) {
                bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
            }
            if (lastHoveredLayer == null || !lastHoveredLayer.equals(screenElement.layerID()) && approvals.isHoverable()) {
                lastHoveredLayer = screenElement.layerID();
                bus.publish(new LayerHoverEvent(screenElement.layerID()));
            }
        }
    }

    private IndexedInteractable findFirstInteractable(MouseInputEvent input, CameraView cameraView) {
        var orderedLayers = getOrderedInteractiveLayers();
        for (LayerID layerID : orderedLayers) {
            if (!isLayerHit(layerID, input.screenPosition, cameraView)) {
                continue;
            }

            var interactable = new IndexedInteractable(layerID, getLayerZIndex(layerID));
            var approvals = checkIfInteractable(world, interactable);

            if (input.eventType == MouseEventTypes.DOWN && approvals.isClickable()) {
                return interactable;
            }

            if (input.eventType == MouseEventTypes.MOVE && approvals.isHoverable()) {
                return interactable;
            }
        }

        return null;
    }

    private ArrayList<LayerID> getOrderedInteractiveLayers() {
        HashSet<LayerID> layerIds = new HashSet<>();
        layerIds.addAll(world.ComponentLayersIndex.query(ClickComponent.class));
        layerIds.addAll(world.ComponentLayersIndex.query(HoverComponent.class));

        ArrayList<LayerID> orderedLayers = new ArrayList<>(layerIds);
        orderedLayers.sort((left, right) -> {
            int zCompare = Integer.compare(getLayerZIndex(right), getLayerZIndex(left));
            if (zCompare != 0) {
                return zCompare;
            }

            return left.Id().compareTo(right.Id());
        });

        return orderedLayers;
    }

    private int getLayerZIndex(LayerID layerID) {
        HashMap<Class<? extends Component>, Component> components = world.Layers.get(layerID);
        if (components == null) {
            return Integer.MIN_VALUE;
        }

        var position = (PositionComponent) components.get(PositionComponent.class);
        return position != null ? position.zIndex : Integer.MIN_VALUE;
    }

    private boolean isLayerHit(LayerID layerID, Point screenPoint, CameraView cameraView) {
        HashMap<Class<? extends Component>, Component> components = world.Layers.get(layerID);
        if (components == null) {
            return false;
        }

        var clickComp = (ClickComponent) components.get(ClickComponent.class);
        var hoverComp = (HoverComponent) components.get(HoverComponent.class);
        var position = (PositionComponent) components.get(PositionComponent.class);
        var dimensions = (DimensionsComponent) components.get(DimensionsComponent.class);
        var tileMap = (TileMapComponent) components.get(TileMapComponent.class);

        if ((clickComp == null && hoverComp == null) || position == null || dimensions == null) {
            return false;
        }

        var strategy = clickComp != null ? clickComp.SelectionStrategy : hoverComp.SelectionStrategy;
        var layerOrigin = layoutManager.getCalculatedScreenPosition(layerID, cameraView);
        if (layerOrigin == null) {
            return false;
        }

        return switch (strategy) {
            case BOUNDING -> isBoundingHit(screenPoint, layerOrigin, dimensions);
            case TILEMAP -> isTileMapHit(screenPoint, layerOrigin, dimensions, tileMap);
        };
    }

    private boolean isBoundingHit(Point screenPoint, Point layerOrigin, DimensionsComponent dimensions) {
        return screenPoint.x() >= layerOrigin.x()
                && screenPoint.x() < layerOrigin.x() + dimensions.width
                && screenPoint.y() >= layerOrigin.y()
                && screenPoint.y() < layerOrigin.y() + dimensions.height;
    }

    private boolean isTileMapHit(Point screenPoint, Point layerOrigin, DimensionsComponent dimensions, TileMapComponent tileMap) {
        if (tileMap == null) {
            return false;
        }

        var tileMapAsset = resources.getAsset(tileMap.resourceId, tileMap.assetId, Cell[][].class);
        if (tileMapAsset == null) {
            return false;
        }

        // Keep iteration order and bounds checks aligned with TileMapRenderPass.
        for (int y = 0; y < dimensions.width; y++) {
            for (int x = 0; x < dimensions.height; x++) {
                if (y >= tileMapAsset.length || x >= tileMapAsset[y].length) {
                    continue;
                }

                Cell cell = tileMapAsset[y][x];
                if (cell == null || cell.content == null) {
                    continue;
                }

                if (screenPoint.equals(new Point(layerOrigin.x() + x, layerOrigin.y() + y))) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onExit(World world) {
        if (mouseInputSubscription != null) {
            mouseInputSubscription.cancel.run();
            mouseInputSubscription = null;
        }

        if (layerRemovedSubscription != null) {
            layerRemovedSubscription.cancel.run();
            layerRemovedSubscription = null;
        }

        focusedLayer = null;
        lastHoveredLayer = null;
    }


    @Override
    public void update(World world, int tickCount) {

    }
}
