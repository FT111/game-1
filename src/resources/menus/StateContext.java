package resources.menus;

import resources.UiBuilders;

import java.util.function.Consumer;

public record StateContext(Consumer<MenuState> switchTo, UiBuilders ui, engine.EventBus bus, MenuStates states) {
}