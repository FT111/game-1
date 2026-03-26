package engine;

import engine_interfaces.objects.ResourceLoader;
import engine_interfaces.objects.rendering.Cell;

import java.util.HashMap;

/// Stores loaded resources (e.g. tile maps, blob data) and their associated loaders for saving
public class Resources {
    protected final HashMap<String, ResourceLoader> resourceLoaders = new HashMap<>();
    protected final HashMap<String, HashMap<String, Object>> loadedResources = new HashMap<>();

    public void addResourceLoader(ResourceLoader loader) {
        resourceLoaders.put(loader.getKey(), loader);
        loadedResources.put(loader.getKey(), loader.Load());
    }

        public HashMap<String, Object> getResource(String key) {
            return loadedResources.get(key);
        }

        public <T> T getAsset(String resourceKey, String assetKey, Class<T> type) {
            Object resource = loadedResources.get(resourceKey).get(assetKey);
            if (type.isInstance(resource)) {
                return type.cast(resource);
            } else {
                throw new IllegalArgumentException("Asset " + resourceKey + "."+ assetKey + " is not of type " + type.getName());
            }
        }

        public boolean hasResource(String key) {
            return loadedResources.containsKey(key);
        }

        public void removeResource(String key) {
            resourceLoaders.remove(key);
            loadedResources.remove(key);
        }

        public void saveResource(String key) {
            ResourceLoader loader = resourceLoaders.get(key);
            if (loader != null && loader.isWritable()) {
                loader.Save();
            } else {
                throw new IllegalArgumentException("Resource " + key + " not found or not savable");
            }
        }

    public void setAsset(String resourceId, String assetId, Object asset) {
        if (!loadedResources.containsKey(resourceId)) {
            throw new IllegalArgumentException("Resource " + resourceId + " not found");
        }
        loadedResources.get(resourceId).put(assetId, asset);
    }
}
