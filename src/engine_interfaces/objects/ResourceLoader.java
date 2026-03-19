package engine_interfaces.objects;

import java.util.HashMap;

public interface ResourceLoader {
    String getKey();
    HashMap<String, Object> Load();
    void Save();
    boolean isWritable();
}
