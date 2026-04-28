package resources.menus;

import engine_interfaces.objects.LayerID;
import resources.UiBuilders;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public record StateContext(Consumer<MenuState> switchTo, UiBuilders ui, engine.EventBus bus, MenuStates states, Function<LayerID, HashMap<Class<? extends engine_interfaces.objects.Component>, engine_interfaces.objects.Component>> elementComponents) {
}