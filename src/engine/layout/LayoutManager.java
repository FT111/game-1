package engine.layout;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.CameraView;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.LayerRegisteredEvent;
import engine_interfaces.objects.events.LayerRemovedEvent;
import engine_interfaces.objects.rendering.PositioningCalculators;

import java.util.HashMap;
import java.util.HashSet;

public class LayoutManager {
    private final World world;
    private final EventBus bus;
    private final Resources resources;

    private final HashMap<LayerID, Point> screenPositionCache = new HashMap<>();
    private CameraView activeCameraViewCache = null;

    private final HashMap<String, SpatialMap> spatialMaps = new HashMap<>();
    private final HashMap<String, MapLayerDefinition> mapDefinitions = new HashMap<>();

    public LayoutManager(World world, EventBus bus, Resources resources) {
        this.world = world;
        this.bus = bus;
        this.resources = resources;

        bus.subscribe(LayerRegisteredEvent.class, () -> true, event -> {
            var e = (LayerRegisteredEvent) event;
            var components = world.Layers.get(e.id);
            if (components == null) return;

            for (var entry : mapDefinitions.entrySet()) {
                if (entry.getValue().matches(components)) {
                    HashSet<Point> extracted = entry.getValue().extractPoints(components, resources);
                    spatialMaps.computeIfAbsent(entry.getKey(), SpatialMap::new).addLayer(e.id, extracted);
                }
            }
        });

        bus.subscribe(LayerRemovedEvent.class, () -> true, event -> {
            var e = (LayerRemovedEvent) event;
            for (SpatialMap spatialMap : spatialMaps.values()) {
                spatialMap.removeLayer(e.id);
            }
        });
    }

    public MapLayerBuilder defineMapLayer(String mapName) {
        return new MapLayerBuilder(mapName, this);
    }

    public void registerMapLayerDefinition(String mapName, MapLayerDefinition definition) {
        mapDefinitions.put(mapName, definition);
    }

    public SpatialMap getSpatialMap(String mapName) {
        return spatialMaps.computeIfAbsent(mapName, SpatialMap::new);
    }

    public void invalidate() {
        screenPositionCache.clear();
        activeCameraViewCache = null;
    }

    public CameraView getActiveCameraView() {
        if (activeCameraViewCache != null) {
            return activeCameraViewCache;
        }

        HashSet<EntityID> cameraEntities = world.ComponentEntitiesIndex.query(CameraComponent.class);
        CameraComponent activeCameraDetails = null;
        PositionComponent activeCameraPosition = null;

        for (EntityID entity : cameraEntities) {
            CameraComponent camera = (CameraComponent) world.Entities.get(entity).get(CameraComponent.class);
            if (camera != null && camera.isActive) {
                if (activeCameraDetails != null) {
                    throw new RuntimeException("Multiple active cameras found in world");
                }
                activeCameraDetails = camera;
                activeCameraPosition = (PositionComponent) world.Entities.get(entity).get(PositionComponent.class);
            }
        }

        if (activeCameraDetails == null || activeCameraPosition == null) {
            // No active camera found
            return null;
        }

        activeCameraViewCache = new CameraView(activeCameraPosition.Origin.x(), activeCameraPosition.Origin.y(), activeCameraDetails.viewWidth, activeCameraDetails.viewHeight);
        return activeCameraViewCache;
    }

    public Point getCalculatedScreenPosition(LayerID layerID, CameraView cameraView) {
        if (screenPositionCache.containsKey(layerID)) {
            return screenPositionCache.get(layerID);
        }

        var components = world.Layers.get(layerID);
        if (components == null) {
            return null;
        }

        var positionComponent = (PositionComponent) components.get(PositionComponent.class);
        if (positionComponent == null) {
            return null;
        }

        Point position = PositioningCalculators.calc.get(positionComponent.positionStrategy)
                .calculatePosition(positionComponent.Origin, layerID, world, cameraView);

        screenPositionCache.put(layerID, position);
        return position;
    }

    public Point getCalculatedScreenPosition(LayerID layerID) {
        CameraView cameraView = getActiveCameraView();
        if (cameraView == null) {
            return null;
        }
        return getCalculatedScreenPosition(layerID, cameraView);
    }
}
