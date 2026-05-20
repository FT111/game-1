package resources.movement.pathfinders;

import engine.layout.SpatialMap;
import engine_interfaces.objects.Point;
import resources.movement.PathfindingStrategy;

import java.util.*;

public class AStarPathfinder implements PathfindingStrategy {
    // Small, tunable penalties to make paths look less "robotic".
    // Keep these small relative to base move cost (1) so guards still take sensible routes.
    private static final int TURN_PENALTY = 1;
    private static final int WALL_HUGGING_PENALTY_PER_ADJACENT_WALL = 1;

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
            if (!(o instanceof PathNode pathNode)) return false;
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

    private boolean isBlocked(Point point, SpatialMap collisionMap) {
        return !collisionMap.getLayersAt(point).isEmpty();
    }

    private int countAdjacentWalls(Point point, SpatialMap collisionMap) {
        int count = 0;
        for (Point n : findNeigbouringPoints(point)) {
            if (isBlocked(n, collisionMap)) count++;
        }
        return count;
    }

    private int turningPenalty(PathNode currentNode, Point nextPoint) {
        if (currentNode.parent == null) return 0; // no previous direction

        Point prev = currentNode.parent;
        int prevDx = currentNode.point.x() - prev.x();
        int prevDy = currentNode.point.y() - prev.y();

        int nextDx = nextPoint.x() - currentNode.point.x();
        int nextDy = nextPoint.y() - currentNode.point.y();

        return (prevDx == nextDx && prevDy == nextDy) ? 0 : TURN_PENALTY;
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
                if (isBlocked(nPoint, collisionMap)) { continue; }

                if (closedSet.contains(nPoint)) { continue; }

                // penalise a cell's cost for being close to a wall, for more natural appearing pathing
                int wallPenalty = WALL_HUGGING_PENALTY_PER_ADJACENT_WALL * countAdjacentWalls(nPoint, collisionMap);

                int turnPenalty = turningPenalty(currentNode, nPoint);
                int stepCost = 1 + wallPenalty + turnPenalty;

                int gCost = currentNode.gCost + stepCost;
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
