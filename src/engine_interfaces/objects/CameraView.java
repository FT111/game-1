package engine_interfaces.objects;

import engine_interfaces.objects.Point;

/// Stores the properties of the camera and related utility functions
/// Derived every tick from the camera object
public class CameraView {
    public int originX;
    public int originY;
    public int width;
    public int height;

    public CameraView(int originX, int originY, int width, int height) {
        this.originX = originX;
        this.originY = originY;
        this.width = width;
        this.height = height;
    }

    public boolean isInView(Point point) {
        return point.x() >= originX && point.x() < originX + width &&
               point.y() >= originY && point.y() < originY + height;
    }

    // Overload for checking if a rectangle defined by two points is in view
    public boolean isInView(Point pointX, Point pointY) {
        return pointX.x() >= originX && pointX.x() < originX + width &&
               pointY.y() >= originY && pointY.y() < originY + height;
    }



    public Point worldToScreen(Point worldPoint) {
        return new Point(worldPoint.x() - originX, worldPoint.y() - originY);
    }
}
