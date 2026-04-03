package engine.systems;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.components.ui.UIElementComponent;
import engine_interfaces.objects.events.ButtonClickEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.events.MouseInputEvent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.ui.IndexedInteractable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UiInteractionSystem extends System {
    private IndexedInteractable focusedLayer = null;
    private AtomicReference<HashMap<Point, IndexedInteractable>> uiElementIndex = new AtomicReference<>(new HashMap<>());
    private HashMap<Point, IndexedInteractable> stagingUiElementIndex = new HashMap<>();
    private HashMap<LayerID, ArrayList<Point>> layerIndexPoints = new HashMap<>();

    public UiInteractionSystem(World world, EventBus bus, Resources resources) {
        bus.subscribe(MouseInputEvent.class,"UiInteractionSystem", event -> {
            var input = (MouseInputEvent) event;
            if (input.eventType != MouseEventTypes.DOWN) {
                return;
            }

            var camera = world.ComponentEntitiesIndex.query(CameraComponent.class).toArray()[0];
            var cameraPos = (PositionComponent) world.Entities.get(camera).get(PositionComponent.class);
//            Point clickWorldPosition = screenToWorldPos(input.screenPosition, cameraPos);
            Point clickWorldPosition = input.screenPosition;

            // check if the click intersects with any UI elements
            var element = uiElementIndex.get().get(clickWorldPosition);
            if (element == null) {
                focusedLayer = null;
                return;
            }

            focusedLayer = new IndexedInteractable(element.layerID(), element.zIndex());
            bus.publish(new ButtonClickEvent(element.layerID()));
        });

        bus.subscribe(LayerRegisteredEvent.class, "UiInteractionSystem", event -> {
            var layerRegisteredEvent = (LayerRegisteredEvent) event;
            var components = world.Layers.get(layerRegisteredEvent.id);
            if (components.containsKey(UIElementComponent.class)) {
                indexUIElement(layerRegisteredEvent.id, world, resources);
            }
        });

        // register all current UI elements
        var uiLayers = world.ComponentLayersIndex.query(UIElementComponent.class);
        uiLayers.forEach(layerID -> indexUIElement(layerID, world, resources));
    }

    public void indexUIElement(LayerID layerID,  World world, Resources resources) {

        // index every cell taken up by UI elements, to allow for O(1) click propagation
        // not ideal for non-grid systems but there's relatively few cells to index here

        // TODO: make a separate index for non static UI elements.
        // Retrieve/index dynamic elements based on movement and camera position

        HashMap<Class<? extends Component>, Component> components = world.Layers.get(layerID);
        var uiElement = (UIElementComponent) components.get(UIElementComponent.class);
        var position = (PositionComponent) components.get(PositionComponent.class);
        var dimensions = (DimensionsComponent) components.get(DimensionsComponent.class);
        var tileMap = (TileMapComponent) components.get(TileMapComponent.class);

        final IndexedInteractable interactable = new IndexedInteractable(layerID, position.zIndex);
        switch (uiElement.SelectionStrategy) {
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

        uiElementIndex.set(stagingUiElementIndex);
    }

    private void removeIndexedUIElement(LayerID layerId) {
        var points = layerIndexPoints.get(layerId);
        if (points == null) {
            return;
        }

        for (Point point : points) {
            stagingUiElementIndex.remove(point);
        }

        uiElementIndex.set(stagingUiElementIndex);
    }

    private void stageElementCell(PositionComponent position, int x, int y, IndexedInteractable interactable) {
        Point cellPoint = new Point(position.Origin.x() + x, position.Origin.y() + y);

        // check for overlapping UI elements and only keep the one with the highest z index
        var existingCellIndex = stagingUiElementIndex.get(cellPoint);
        if (existingCellIndex != null && existingCellIndex.zIndex() > position.zIndex) {
            return;
        }
        stagingUiElementIndex.put(cellPoint, interactable);
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
