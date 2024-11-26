package chime.calculator;

import chime.calculator.config.PathConfig;
import chime.calculator.util.Node;

import java.util.*;

public class PathFinder {

    public static List<Node> findPath(PathConfig.Walk config) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::getTotalCost));
        Set<Node> closed = new HashSet<>();

        Node endNode = new Node(config.end);
        Node startNode = new Node(config.start, endNode);

        open.add(startNode);

        int i = 0;
        Node bestNode = startNode;

        while (!open.isEmpty() && i < config.iterations) {
            Node current = open.poll();
            closed.add(current);

            if (current.getHCost() < bestNode.getHCost()) {
                bestNode = current;
            }

            if (current.equals(endNode)) {
                return getPath(current);
            }

            populateNeighbours(open, closed, current, endNode);
            i++;
        }

        return config.longDistance && !config.start.equals(config.end) ? getPath(bestNode) : new ArrayList<>();
    }

    private static void populateNeighbours(
            PriorityQueue<Node> openQueue,
            Set<Node> closedList,
            Node current,
            Node endNode
    ) {
        List<Node> neighbours = new ArrayList<>();
        
        neighbours.add(new Node(-1, 0, 0, current, endNode));
        neighbours.add(new Node(1, 0, 0, current, endNode));
        neighbours.add(new Node(0, 0, -1, current, endNode));
        neighbours.add(new Node(0, 0, 1, current, endNode));
        neighbours.add(new Node(0, 1, 0, current, endNode));
        neighbours.add(new Node(0, -1, 0, current, endNode));
        neighbours.add(new Node(1, 0, 1, current, endNode));
        neighbours.add(new Node(-1, 0, -1, current, endNode));
        neighbours.add(new Node(1, 0, -1, current, endNode));
        neighbours.add(new Node(-1, 0, 1, current, endNode));

        for (Node neighbour : neighbours) {
            if (closedList.contains(neighbour)) continue;
            
            if (neighbour.canBeTraversed()) {
                if (neighbour.getTotalCost() < current.getTotalCost() || !openQueue.contains(neighbour)) {
                    openQueue.remove(neighbour);
                    openQueue.add(neighbour);
                }
            }
        }
    }

    private static List<Node> getPath(Node currentNode) {
        List<Node> path = new ArrayList<>();
        path.add(currentNode);
        Node parent;

        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }

        return path;
    }

}
