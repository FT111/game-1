package engine_interfaces.objects;

import java.util.Dictionary;
import java.util.HashMap;

public interface ResourceLoader {
    Object Load(HashMap<String, Object> params);
    void Save();
    boolean Saveable();
}
