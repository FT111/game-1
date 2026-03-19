package engine;

import engine_interfaces.objects.ResourceLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Resources {
    protected final List<ResourceLoader> resourceLoaders = new ArrayList<>();
    protected final HashMap<String, Object> loadedResources = new HashMap<>();

    public void addResourceLoader(String key, ResourceLoader loader) {
        resourceLoaders.add(loader);
        loadedResources.put(key, loader.Load());
    }

        public Object getResource(String key) {
            return loadedResources.get(key);
        }

        public void saveResource

}
