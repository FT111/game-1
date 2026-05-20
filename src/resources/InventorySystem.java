package resources;

import engine.EventBus;
import engine.Logs;
import engine.World;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.MovementEvent;
import engine_interfaces.objects.components.PositionComponent;
import resources.components.ItemComponent;
import resources.components.InventoryComponent;
import resources.components.PlayerComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.rendering.Colour;
import resources.UiBuilders;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.EventSubscriptionReceipt;
import engine_interfaces.objects.events.NewGameEvent;
import engine_interfaces.objects.events.ButtonClickEvent;
import engine_interfaces.objects.ResourceLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InventorySystem extends System {
    private final EventBus bus;
    private final engine.Resources resources;
    private LayerID uiLayerID;
    private LayerID descriptionLabelID;
    private List<LayerID> itemLayers = new ArrayList<>();
    private EventSubscriptionReceipt movementSubscription;
    private EventSubscriptionReceipt newGameSubscription;
    private EventSubscriptionReceipt clickSubscription;
    private Map<LayerID, InventoryComponent.Item> layerToItemMap = new HashMap<>();

    public InventorySystem(EventBus bus, engine.Resources resources) {
        this.bus = bus;
        this.resources = resources;
    }

    @Override
    public void onEnter(World world) {
        if (!resources.hasResource("gameState")) {
            resources.addResourceLoader(new ResourceLoader() {
                private final HashMap<String, Object> data = new HashMap<>();
                @Override public String getKey() { return "gameState"; }
                @Override public HashMap<String, Object> Load() { return data; }
                @Override public void Save() {}
                @Override public boolean isWritable() { return false; }
            });
        }
        int containerWidth = 80;
        int maxWidth = containerWidth -2;

        UiBuilders ui = new UiBuilders(world);
        var labelBuilder = ui.new LabelBuilder();
        labelBuilder.withPosition(new Point(0, 0), Positioning.FIXED)
                .withZIndex(20)
                .withDimensions(containerWidth, 8)
                .withAlignment(Alignment.BOTTOM_LEFT)
                .withBorder('-', '|', '+', new Colour(255, 255, 255))
                .withStaticText("Inventory \n");
        uiLayerID = labelBuilder.build();

        var descUiBuilder = ui.new LabelBuilder();
        descUiBuilder.withPosition(new Point(2, 0), Positioning.RELATIVE)
                .withZIndex(30)
                .withAlignment(Alignment.BOTTOM_LEFT)
                .withDimensions(maxWidth, 3)
                .withStaticText("")
                .withParent(uiLayerID);

        descriptionLabelID = descUiBuilder.build();
        world.getLayerComponent(descriptionLabelID, TextComponent.class).maxWidth = maxWidth;
        world.getLayerComponent(descriptionLabelID, TextComponent.class).maxWidth = maxWidth;


        world.Layers.get(uiLayerID).put(VisibilityComponent.class, new VisibilityComponent(true));
        world.Layers.get(descriptionLabelID).put(VisibilityComponent.class, new VisibilityComponent(true));

        movementSubscription = bus.subscribe(MovementEvent.class, () -> isEnabled, event -> {
            if (!(event instanceof MovementEvent moveEvent)) {
                return;
            }

            if (world.Entities.get(moveEvent.entityID) == null || !world.Entities.get(moveEvent.entityID).containsKey(PlayerComponent.class)) {
                return;
            }
            if (!world.Entities.get(moveEvent.entityID).containsKey(InventoryComponent.class)) {
                return;
            }
            InventoryComponent inventory = world.getEntityComponent(moveEvent.entityID, InventoryComponent.class);

            HashSet<EntityID> items = world.ComponentEntitiesIndex.query(ItemComponent.class);
            if (items == null || items.isEmpty()) return;

            EntityID pickedItem = null;
            for (EntityID entityID : items) {
                if (entityID.equals(moveEvent.entityID)) continue;
                PositionComponent pos = world.getEntityComponent(entityID, PositionComponent.class);
                if (pos.Origin.equals(moveEvent.newOrigin)) {
                    ItemComponent item = world.getEntityComponent(entityID, ItemComponent.class);
                    inventory.items.add(new InventoryComponent.Item(item.itemName, item.description));
                    Logs.log("Picked up item: " + item.itemName);
                    pickedItem = entityID;
                    updateInventoryUI(world, inventory.items);
                    break;
                }
            }

            if (pickedItem != null) {
                world.removeEntity(pickedItem);
            }
        });

        newGameSubscription = bus.subscribe(NewGameEvent.class, () -> isEnabled, event -> {
            HashSet<EntityID> players = world.ComponentEntitiesIndex.query(PlayerComponent.class);
            if (players != null) {
                for (EntityID player : players) {
                    InventoryComponent inv = world.getEntityComponent(player, InventoryComponent.class);
                    if (inv != null) {
                        inv.items.clear();
                        updateInventoryUI(world, inv.items);
                    }
                }
            }
        });

        clickSubscription = bus.subscribe(ButtonClickEvent.class, () -> isEnabled, event -> {
            if (!(event instanceof ButtonClickEvent btnEvent)) {
                return;
            }
            InventoryComponent.Item clickedItem = layerToItemMap.get(btnEvent.buttonLayerId);
            if (clickedItem != null) {
                resources.setAsset("gameState", "selectedItem", clickedItem.itemName);
                TextComponent descText = world.getLayerComponent(descriptionLabelID, TextComponent.class);
                if (descText != null) {
                    descText.text = "§ "+ clickedItem.itemName + " - " + clickedItem.description;
                    world.Layers.get(descriptionLabelID).put(TextComponent.class, descText);
                }
            }
        });
    }
    
    @Override
    public void onExit(World world) {
        if (uiLayerID != null) {
            world.removeLayer(uiLayerID);
            world.removeLayer(descriptionLabelID);
            for (LayerID layer : itemLayers) {
                world.removeLayer(layer);
            }
            uiLayerID = null;
            descriptionLabelID = null;
            itemLayers.clear();
        }
        if (movementSubscription != null) {
            movementSubscription.cancel.run();
            movementSubscription = null;
        }
        if (newGameSubscription != null) {
            newGameSubscription.cancel.run();
            newGameSubscription = null;
        }
        if (clickSubscription != null) {
            clickSubscription.cancel.run();
            clickSubscription = null;
        }
    }

    private void updateInventoryUI(World world, List<InventoryComponent.Item> items) {
        if (uiLayerID == null) return;

        for (LayerID oldLayer : itemLayers) {
            world.removeLayer(oldLayer);
        }
        itemLayers.clear();
        layerToItemMap.clear();

        UiBuilders ui = new UiBuilders(world);

        int currentX = 2;
        int currentY = 1;
        for (InventoryComponent.Item item : items) {
            int width = item.itemName.length() + 2;
            if (currentX + width > 48) {
                currentX = 2;
                currentY++;
            }

            var btnBuilder = ui.new ButtonBuilder()
                    .withPosition(new Point(currentX, currentY), Positioning.RELATIVE)
                    .withZIndex(30)
                    .withDimensions(width, 1)
                    .withStaticText("[" + item.itemName + "]")
                    .withParent(uiLayerID);

            LayerID btnLayer = btnBuilder.build();
            world.Layers.get(btnLayer).put(VisibilityComponent.class, new VisibilityComponent(true));
            itemLayers.add(btnLayer);
            layerToItemMap.put(btnLayer, item);

            currentX += width;
        }
    }

    @Override
    public void update(World world, int tickCount) {
    }
}
