package game.components;

import engine_interfaces.objects.Component;

import java.util.HashSet;

public class VisionBlockerComponent extends Component {
    public HashSet<Character> blockingTiles;

    public VisionBlockerComponent(HashSet<Character> blockingTiles) {
        this.blockingTiles = blockingTiles;
    }
}
