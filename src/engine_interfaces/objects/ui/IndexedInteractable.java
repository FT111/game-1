package engine_interfaces.objects.ui;

import engine_interfaces.objects.LayerID;

public record IndexedInteractable(LayerID layerID,  int zIndex) {
}
