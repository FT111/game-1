package tests.engine;

import engine.Resources;
import engine_interfaces.objects.rendering.Cell;
import org.junit.jupiter.api.Test;
import tests.testsupport.TestResourceLoader;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResourcesTest {

    @Test
    void getAssetReturnsTypedResource() {
        Resources resources = new Resources();
        HashMap<String, Object> assets = new HashMap<>();
        Cell[][] map = new Cell[][]{{new Cell('#')}};
        assets.put("map", map);
        resources.addResourceLoader(new TestResourceLoader("tiles", assets, false));

        Cell[][] result = resources.getAsset("tiles", "map", Cell[][].class);

        assertEquals('#', result[0][0].content);
    }

    @Test
    void getAssetThrowsOnTypeMismatch() {
        Resources resources = new Resources();
        HashMap<String, Object> assets = new HashMap<>();
        assets.put("map", "not-a-map");
        resources.addResourceLoader(new TestResourceLoader("tiles", assets, false));

        assertThrows(IllegalArgumentException.class, () -> resources.getAsset("tiles", "map", Cell[][].class));
    }

    @Test
    void setAssetRequiresExistingResource() {
        Resources resources = new Resources();

        assertThrows(IllegalArgumentException.class, () -> resources.setAsset("missing", "asset", new Object()));
    }

    @Test
    void saveResourceThrowsWhenNotWritable() {
        Resources resources = new Resources();
        resources.addResourceLoader(new TestResourceLoader("tiles", new HashMap<>(), false));

        assertThrows(IllegalArgumentException.class, () -> resources.saveResource("tiles"));
    }
}
