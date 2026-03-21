package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

import java.util.HashSet;

public class LayerColliderComponent extends Component {
    public HashSet<Character> collidableTiles;

    public LayerColliderComponent(HashSet<Character> collidableTiles) {
        this.collidableTiles = collidableTiles;
    }
}
