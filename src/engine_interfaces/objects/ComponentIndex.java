package engine_interfaces.objects;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ComponentIndex<ID> {
    private final HashMap<Class<? extends Component>, List<ID>> index = new HashMap<>();

    public void add(ID id, Class<? extends Component> componentClass) {
        index.computeIfAbsent(componentClass, k -> new ArrayList<>()).add(id);
    }

    public void remove(ID id, Class<? extends Component> componentClass) {
        List<ID> ids = index.get(componentClass);
        if (ids != null) ids.remove(id);
    }

    public HashSet<ID> query(Class<? extends Component>[] required) {
        if (required.length == 0) return new HashSet<>();

        HashSet<ID> result = null;
        for (Class<? extends Component> c : required) {
            List<ID> ids = index.getOrDefault(c, List.of());
            if (ids.isEmpty()) return new HashSet<>();

            if (result == null) {
                result = new HashSet<>(ids);
            } else {
                result.retainAll(ids);
            }
        }
        return result;
    }

    public HashSet<ID> query(Class<? extends Component> required) {
        return new HashSet<>(index.getOrDefault(required, List.of()));
    }
}