package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;

/**
 * Component to mark a layer/entity as only visible where a given vision layer shows visibility.
 * The specified vision layer should be updated by the VisionSystem (it should contain a TileMap asset
 * with non-null cells at visible locations).
 */
public class MaskToTileMapComponent extends Component {
    public LayerID visionLayer;

    public MaskToTileMapComponent(LayerID visionLayer) {
        this.visionLayer = visionLayer;
    }
}

