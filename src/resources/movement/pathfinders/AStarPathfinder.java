package resources.movement.pathfinders;

import engine.layout.SpatialMap;
import engine_interfaces.objects.Point;
import resources.movement.PathfindingStrategy;

import java.util.*;

public class AStarPathfinder implements PathfindingStrategy {
    private static class PathNode implements Comparable<PathNode> {
        Point point;
        Point parent;
        int gCost; // cost from start to this node
        int hCost; // heuristic cost from this node to end
        int fCost; // total cost (gCost + hCost)

        public PathNode(Point point, Point parent, int gCost, int hCost, int fCost) {
            this.point = point;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = fCost;
        }


        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(this.fCost, other.fCost);
        }

        @Override
        public int hashCode() {
            return Objects.hash(point);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathNode pathNode = (PathNode) o;
            return Objects.equals(point, pathNode.point);
        }
    }

    private int findSquaredDistance(Point a, Point b) {
        int dx = a.x() - b.x();
        int dy = a.y() - b.y();
        return dx * dx + dy * dy;
    }

    // For a 4-connected grid (up/down/left/right) the Manhattan distance is
    // an admissible heuristic (each move costs 1). Use this instead of
    // squared Euclidean which can overestimate and make A* non-optimal.
    private int findManhattanDistance(Point a, Point b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    private Point[] findNeigbouringPoints(Point point) {
        return new Point[] {
            new Point(point.x() + 1, point.y()),
            new Point(point.x() - 1, point.y()),
            new Point(point.x(), point.y() + 1),
            new Point(point.x(), point.y() - 1)
        };
    }

    private List<Point> reconstructPath(PathNode finalNode, HashMap<Point, PathNode> allNodes) {
        LinkedList<Point> path = new LinkedList<>();
        PathNode currentNode = finalNode;
        while (currentNode != null) {
            path.addFirst(currentNode.point);
            if (currentNode.parent == null) {
                return path;
            }
            currentNode = allNodes.get(currentNode.parent);
        }
        return path;
    }

    @Override
    public List<Point> findPath(Point start, Point end, SpatialMap collisionMap) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();
        HashMap<Point, PathNode> allNodes = new HashMap<>();

        // initialise start node correctly (f = g + h)
        int startH = findManhattanDistance(start, end);
        PathNode startNode = new PathNode(start, null, 0, startH, startH);
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            var currentNode = openSet.poll();

            // skip stale entries if we've already found a better path to this point
            var recorded = allNodes.get(currentNode.point);
            if (recorded != null && currentNode.gCost != recorded.gCost) {
                continue; // stale entry
            }

            // mark as visited
            if (closedSet.contains(currentNode.point)) {
                continue;
            }
            closedSet.add(currentNode.point);

            // we've hit the final point, return the final path by backtracking through parents
            if (currentNode.point.equals(end)) {
                return reconstructPath(currentNode, allNodes);
            }

            // explore neighbours
            for (Point nPoint : findNeigbouringPoints(currentNode.point)) {
                if (!collisionMap.getLayersAt(nPoint).isEmpty()) { continue; }

                if (closedSet.contains(nPoint)) { continue; }

                int gCost = currentNode.gCost + 1;
                int hCost = findManhattanDistance(nPoint, end);
                int fCost = gCost + hCost;

                var existing = allNodes.get(nPoint);
                if (existing == null || gCost < existing.gCost) {
                    PathNode childNode = new PathNode(nPoint, currentNode.point, gCost, hCost, fCost);
                    allNodes.put(nPoint, childNode);
                    openSet.add(childNode);
                }
            }
        }

        // no path found
        return Collections.emptyList();
    }
}
