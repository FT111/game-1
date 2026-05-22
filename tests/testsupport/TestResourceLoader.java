package tests.testsupport;

import engine_interfaces.objects.ResourceLoader;

import java.util.HashMap;

public class TestResourceLoader implements ResourceLoader {
    private final String key;
    private final HashMap<String, Object> resources;
    private final boolean writable;

    public TestResourceLoader(String key, HashMap<String, Object> resources, boolean writable) {
        this.key = key;
        this.resources = resources;
        this.writable = writable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public HashMap<String, Object> Load() {
        return resources;
    }

    @Override
    public void Save() {
        if (!writable) {
            throw new IllegalStateException("resource loader is not writable");
        }
    }

    @Override
    public boolean isWritable() {
        return writable;
    }
}
