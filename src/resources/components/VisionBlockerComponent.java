package resources.components;

import engine_interfaces.objects.Component;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

public class VisionBlockerComponent extends Component {
    public HashSet<Character> blockingTiles;

    public VisionBlockerComponent(HashSet<Character> blockingTiles) {
        this.blockingTiles = blockingTiles;
    }
}
