package engine_interfaces.objects;

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

    public boolean isWorldPointInView(Point point) {
        return point.x() >= originX && point.x() < originX + width &&
               point.y() >= originY && point.y() < originY + height;
    }

    // Overload for checking if a rectangle defined by two points is in view
    public boolean isWorldPointInView(Point pointX, Point pointY) {
        // return true if the rectangles [pointX, pointY) and [origin, origin+size) overlap
        return pointX.x() < originX + width && pointY.x() > originX &&
               pointX.y() < originY + height && pointY.y() > originY;
    }

    public boolean isScreenPointInView(Point point) {
        return point.x() >= 0 && point.x() < width &&
               point.y() >= 0 && point.y() < height;
    }

    public boolean isScreenPointInView(Point pointX, Point pointY) {
        // same overlap logic but against the screen rectangle [0,0,width,height)
        return pointX.x() < width && pointY.x() > 0 &&
               pointX.y() < height && pointY.y() > 0;
    }



    public Point worldToScreen(Point worldPoint) {
        return new Point(worldPoint.x() - originX, worldPoint.y() - originY);
    }
}
