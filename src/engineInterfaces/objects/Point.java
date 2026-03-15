package engineInterfaces.objects;

/// Represents a point in 2D space.
public record Point(int x, int y) {
        public Point add(Point other) {
            return new Point(this.x + other.x, this.y + other.y);
        }

        public Point subtract(Point other) {
            return new Point(this.x - other.x, this.y - other.y);
        }

        public Point multiply(int scalar) {
            return new Point(this.x * scalar, this.y * scalar);
        }

        public Point divide(int scalar) {
            return new Point(this.x / scalar, this.y / scalar);
        }
}
