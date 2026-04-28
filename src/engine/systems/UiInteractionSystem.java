package engine.systems;

import engine.EventBus;
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
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.events.MouseInputEvent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.ui.IndexedInteractable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class UiInteractionSystem extends System {
    private IndexedInteractable focusedLayer = null;
    private LayerID lastHoveredLayer = null;
    private final World world;
    private final EventBus bus;
    private final Resources resources;
    private final AtomicReference<HashMap<Point, IndexedInteractable>> staticUiElementIndex = new AtomicReference<>(new HashMap<>()); // Screen points
    private final AtomicReference<HashMap<Point, IndexedInteractable>> dynamicUiElementIndex = new AtomicReference<>(new HashMap<>()); // World points;
    private final HashMap<Point, IndexedInteractable> stagingStaticElementIndex = new HashMap<>();
    private final HashMap<Point, IndexedInteractable> stagingDynamicElementIndex = new HashMap<>();
    private final HashMap<LayerID, ArrayList<Point>> layerIndexPoints = new HashMap<>();
    private final HashMap<LayerID, Positioning> layerIndexPositioning = new HashMap<>();

    private EventSubscriptionReceipt mouseInputSubscription;
    private EventSubscriptionReceipt layerRegisteredSubscription;
    private EventSubscriptionReceipt layerRemovedSubscription;

    public UiInteractionSystem(World world, EventBus bus, Resources resources) {
        this.world = world;
        this.bus = bus;
        this.resources = resources;
    }

    @Override
    public void onEnter(World world) {
        mouseInputSubscription = bus.subscribe(MouseInputEvent.class, () -> isEnabled, event -> {
            var input = (MouseInputEvent) event;
            if (input.eventType != MouseEventTypes.DOWN && input.eventType != MouseEventTypes.MOVE) {
                return;
            }

            var camera = world.ComponentEntitiesIndex.query(CameraComponent.class).toArray()[0];
            var cameraPos = (PositionComponent) world.Entities.get(camera).get(PositionComponent.class);
            Point clickWorldPosition = screenToWorldPos(input.screenPosition, cameraPos);

            // not elegant but prioritises screen element intersecting clicks over world clicks
            var screenElement = staticUiElementIndex.get().get(input.screenPosition);
            if (screenElement != null) {
                if (input.eventType == MouseEventTypes.DOWN) {
                    focusedLayer = screenElement;
                    bus.publish(new ButtonClickEvent(screenElement.layerID()));
                } else if (input.eventType == MouseEventTypes.MOVE) {
                    if (lastHoveredLayer != null && !lastHoveredLayer.equals(screenElement.layerID())) {
                        bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
                    }
                    if (lastHoveredLayer == null || !lastHoveredLayer.equals(screenElement.layerID())) {
                        lastHoveredLayer = screenElement.layerID();
                        bus.publish(new LayerHoverEvent(screenElement.layerID()));
                    }
                }
                return;
            }

            // check if the click intersects with any UI elements
            var worldElement = dynamicUiElementIndex.get().get(clickWorldPosition);
            if (worldElement == null) {
                if (input.eventType == MouseEventTypes.DOWN) {
                    focusedLayer = null;
                } else if (input.eventType == MouseEventTypes.MOVE && lastHoveredLayer != null) {
                    bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
                    lastHoveredLayer = null;
                }
                return;
            }

            if (input.eventType == MouseEventTypes.DOWN) {
                focusedLayer = worldElement;
                bus.publish(new ButtonClickEvent(worldElement.layerID()));
            } else if (input.eventType == MouseEventTypes.MOVE) {
                if (lastHoveredLayer != null && !lastHoveredLayer.equals(worldElement.layerID())) {
                    bus.publish(new LayerHoverExitEvent(lastHoveredLayer));
                }
                if (lastHoveredLayer == null || !lastHoveredLayer.equals(worldElement.layerID())) {
                    lastHoveredLayer = worldElement.layerID();
                    bus.publish(new LayerHoverEvent(worldElement.layerID()));
                }
            }
        });

        layerRegisteredSubscription = bus.subscribe(LayerRegisteredEvent.class, () -> isEnabled, event -> {
            var layerRegisteredEvent = (LayerRegisteredEvent) event;
            var components = world.Layers.get(layerRegisteredEvent.id);
            if (components != null && (components.containsKey(ClickComponent.class) || components.containsKey(HoverComponent.class))) {
                indexUIElement(layerRegisteredEvent.id, world, resources);
            }
        });

        layerRemovedSubscription = bus.subscribe(LayerRemovedEvent.class, () -> isEnabled, event -> {
            var layerRemovedEvent = (LayerRemovedEvent) event;
            removeIndexedUIElement(layerRemovedEvent.id);
        });

        stagingStaticElementIndex.clear();
        stagingDynamicElementIndex.clear();
        layerIndexPoints.clear();
        layerIndexPositioning.clear();

        // Register currently present UI elements.
        var uiLayers = world.ComponentLayersIndex.query(ClickComponent.class);
        uiLayers.forEach(layerID -> indexUIElement(layerID, world, resources));
        var hoverLayers = world.ComponentLayersIndex.query(HoverComponent.class);
        hoverLayers.forEach(layerID -> indexUIElement(layerID, world, resources));
    }

    @Override
    public void onExit(World world) {
        if (mouseInputSubscription != null) {
            mouseInputSubscription.cancel.run();
            mouseInputSubscription = null;
        }

        if (layerRegisteredSubscription != null) {
            layerRegisteredSubscription.cancel.run();
            layerRegisteredSubscription = null;
        }

        if (layerRemovedSubscription != null) {
            layerRemovedSubscription.cancel.run();
            layerRemovedSubscription = null;
        }

        focusedLayer = null;
        lastHoveredLayer = null;
        stagingStaticElementIndex.clear();
        stagingDynamicElementIndex.clear();
        layerIndexPoints.clear();
        layerIndexPositioning.clear();
        staticUiElementIndex.set(new HashMap<>());
        dynamicUiElementIndex.set(new HashMap<>());
    }

    public void indexUIElement(LayerID layerID,  World world, Resources resources) {

        // index every cell taken up by UI elements, to allow for O(1) click propagation
        // not ideal for non-grid systems but there's relatively few cells to index here

        // TODO: make a separate index for non static UI elements.
        // Retrieve/index dynamic elements based on movement and camera position

        HashMap<Class<? extends Component>, Component> components = world.Layers.get(layerID);
        if (components == null) {
            return;
        }

        var clickComp = (ClickComponent) components.get(ClickComponent.class);
        var hoverComp = (HoverComponent) components.get(HoverComponent.class);
        var position = (PositionComponent) components.get(PositionComponent.class);
        var dimensions = (DimensionsComponent) components.get(DimensionsComponent.class);
        var tileMap = (TileMapComponent) components.get(TileMapComponent.class);

        if ((clickComp == null && hoverComp == null) || position == null || dimensions == null) {
            return;
        }

        engine_interfaces.objects.ui.SelectionStrategies strategy = clickComp != null ? clickComp.SelectionStrategy : hoverComp.SelectionStrategy;

        // Prevent duplicates when re-registering an existing layer.
        removeIndexedUIElement(layerID);

        final IndexedInteractable interactable = new IndexedInteractable(layerID, position.zIndex);
        layerIndexPositioning.put(layerID, position.positionStrategy);
        switch (strategy) {
            case BOUNDING -> {
                for (int x = 0; x < dimensions.width; x++) {
                    for (int y = 0; y < dimensions.height; y++) {
                        stageElementCell(position, x, y, interactable);
                        var pointsArr = layerIndexPoints.computeIfAbsent(layerID, k -> new ArrayList<>());
                        pointsArr.add(new Point(position.Origin.x() + x, position.Origin.y() + y));
                    }
                }
            }
            case TILEMAP -> {
                if (tileMap == null) {throw new IllegalArgumentException("Tilemap selection strategy requires a tilemap component");}

                var tileMapAsset = resources.getAsset(tileMap.resourceId, tileMap.assetId, Cell[][].class);
                for (int i = 0; i < tileMapAsset.length; i++) {
                    Cell[] cells = tileMapAsset[i];
                    for (int j = 0; j < cells.length; j++) {
                        Cell cell = cells[j];
                        if (cell != null && cell.content != null) {
                            stageElementCell(position, j, i, interactable);
                        }
                    }
                }
        }}

        if (position.positionStrategy.equals(Positioning.FIXED)) {
            staticUiElementIndex.set(stagingStaticElementIndex);
        } else {
            dynamicUiElementIndex.set(stagingDynamicElementIndex);
        }
    }

    private void removeIndexedUIElement(LayerID layerId) {
        var points = layerIndexPoints.get(layerId);
        var positioning = layerIndexPositioning.get(layerId);

        if (points == null || positioning == null) {
            layerIndexPoints.remove(layerId);
            layerIndexPositioning.remove(layerId);
            return;
        }

        var index = (positioning.equals(Positioning.FIXED)) ? stagingStaticElementIndex : stagingDynamicElementIndex;

        for (Point point : points) {
            index.remove(point);
        }

        if (positioning.equals(Positioning.FIXED)) {
            staticUiElementIndex.set(stagingStaticElementIndex);
        } else {
            dynamicUiElementIndex.set(stagingDynamicElementIndex);
        }

        layerIndexPoints.remove(layerId);
        layerIndexPositioning.remove(layerId);
    }

    private void stageElementCell(PositionComponent position, int x, int y, IndexedInteractable interactable) {
        Point cellPoint = new Point(position.Origin.x() + x, position.Origin.y() + y);

        // check for overlapping UI elements and only keep the one with the highest z index
        var existingCellIndex = stagingStaticElementIndex.get(cellPoint);
        if (existingCellIndex != null && existingCellIndex.zIndex() > position.zIndex) {
            return;
        }
        stagingStaticElementIndex.put(cellPoint, interactable);
    }

    public Point screenToWorldPos(Point screenPoint, PositionComponent cameraPosition) {
        return new Point(
            screenPoint.x() + cameraPosition.Origin.x(),
            screenPoint.y() + cameraPosition.Origin.y()
        );
    }

    @Override
    public void update(World world, int tickCount) {

    }
}
