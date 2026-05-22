package game;

import engine.CoreWorld;
import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.EventSubscriptionReceipt;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.events.CollisionEvent;
import engine_interfaces.objects.rendering.Cell;
import game.components.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DoorSystem extends System {
    private static final String DOOR_RESOURCE_ID = "door-assets";

    private final Resources resources;
    private final EventBus bus;
    private EventSubscriptionReceipt doorSubscription;

    public DoorSystem(Resources resources, EventBus bus) {
        this.resources = resources;
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        ensureDoorResource();
        doorSubscription = bus.subscribe(CollisionEvent.class, () -> isEnabled, event -> {
            if (!(event instanceof CollisionEvent collisionEvent)) {
                return;
            }

            DoorComponent door = world.getLayerComponent(collisionEvent.collidedWithId, DoorComponent.class);
            if (door == null || door.isOpen || !door.canBeOpened) {
                return;
            }

            if (world.getEntityComponent(collisionEvent.entityId, PlayerComponent.class) == null) {
                return;
            }

            InventoryComponent inventory = world.getEntityComponent(collisionEvent.entityId, InventoryComponent.class);
            if (door.requiredItemName != null && !hasItem(inventory, door.requiredItemName)) {
                return;
            }

            openDoor(world, collisionEvent.collidedWithId, door);
        });
    }

    @Override
    public void onExit(World world) {
        if (doorSubscription != null) {
            doorSubscription.cancel.run();
            doorSubscription = null;
        }
    }

    public LayerID registerDoor(CoreWorld world, Point origin, DoorComponent door) {
        ensureDoorResource();

        String assetId = "door-" + UUID.randomUUID();
        resources.setAsset(DOOR_RESOURCE_ID, assetId, buildDoorTileMap(door.closedCharacters, door));

        int width = door.orientation == DoorOrientation.HORIZONTAL ? door.width : 1;
        int height = door.orientation == DoorOrientation.VERTICAL ? door.width : 1;

        ArrayList<Component> components = new ArrayList<>();
        components.add(door);
        components.add(new PositionComponent(origin, 1));
        components.add(new DimensionsComponent(width, height));
        components.add(new VisibilityComponent(true));
        components.add(new TileMapComponent(DOOR_RESOURCE_ID, assetId, "tl", false));

        return world.createLayer(components.toArray(new Component[0]));
    }

    private void openDoor(CoreWorld world, LayerID doorLayerId, DoorComponent door) {
        door.isOpen = true;
        var tileMap = world.getLayerComponent(doorLayerId, TileMapComponent.class);
        if (tileMap != null) {
            resources.setAsset(DOOR_RESOURCE_ID, tileMap.assetId, buildDoorTileMap(door.openCharacters, door));
        }
    }

    private static boolean hasItem(InventoryComponent inventory, String requiredItemName) {
        if (inventory == null || requiredItemName == null) {
            return false;
        }

        for (InventoryComponent.Item item : inventory.items.values()) {
            if (requiredItemName.equals(item.itemName)) {
                return true;
            }
        }
        return false;
    }

    private void ensureDoorResource() {
        if (resources.hasResource(DOOR_RESOURCE_ID)) {
            return;
        }
        resources.addResourceLoader(new engine_interfaces.objects.ResourceLoader() {
            @Override
            public String getKey() {
                return DOOR_RESOURCE_ID;
            }

            @Override
            public HashMap<String, Object> Load() {
                return new HashMap<>();
            }

            @Override
            public void Save() {
            }

            @Override
            public boolean isWritable() {
                return false;
            }
        });
    }

    private static Cell[][] buildDoorTileMap(char[] characters, DoorComponent door) {
        if (door.orientation == DoorOrientation.HORIZONTAL) {
            Cell[][] map = new Cell[1][door.width];
            for (int x = 0; x < door.width; x++) {
                map[0][x] = new Cell(characters[x % characters.length], null, null);
            }
            return map;
        }

        Cell[][] map = new Cell[door.width][1];
        for (int y = 0; y < door.width; y++) {
            map[y][0] = new Cell(characters[y % characters.length], null, null);
        }
        return map;
    }
}
