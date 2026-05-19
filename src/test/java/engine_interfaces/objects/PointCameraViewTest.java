package engine_interfaces.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PointCameraViewTest {

    @Test
    void pointMathWorks() {
        Point a = new Point(6, 4);
        Point b = new Point(2, 1);

        assertEquals(new Point(8, 5), a.add(b));
        assertEquals(new Point(4, 3), a.subtract(b));
        assertEquals(new Point(12, 8), a.multiply(2));
        assertEquals(new Point(3, 2), a.divide(2));
        assertEquals(5.0, new Point(3, 4).magnitude());
    }

    @Test
    void cameraViewChecksWorldAndScreenBounds() {
        CameraView camera = new CameraView(10, 10, 5, 5);

        assertTrue(camera.isWorldPointInView(new Point(10, 10)));
        assertFalse(camera.isWorldPointInView(new Point(15, 10)));
        assertTrue(camera.isWorldPointInView(new Point(9, 9), new Point(12, 12)));
        assertFalse(camera.isWorldPointInView(new Point(0, 0), new Point(5, 5)));

        assertTrue(camera.isScreenPointInView(new Point(0, 0)));
        assertFalse(camera.isScreenPointInView(new Point(5, 5)));
    }

    @Test
    void worldToScreenAppliesOrigin() {
        CameraView camera = new CameraView(10, 10, 5, 5);

        assertEquals(new Point(2, 3), camera.worldToScreen(new Point(12, 13)));
    }
}
