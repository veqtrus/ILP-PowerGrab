package uk.ac.ed.inf.powergrab.search;

import java.util.*;

public class IterativeTspSolver<N extends TspSolver.Node<N>> implements TspSolver<N> {
    private int maxIterations;
    private N initialNode;

    public IterativeTspSolver() {
        maxIterations = Integer.MAX_VALUE;
        initialNode = null;
    }

    public IterativeTspSolver(int maxIterations) {
        setMaxIterations(maxIterations);
        initialNode = null;
    }

    public int getMaxIterations() { return maxIterations; }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 0) throw new IllegalArgumentException();
        this.maxIterations = maxIterations;
    }

    @Override
    public N getInitialNode() { return initialNode; }

    @Override
    public void setInitialNode(N initialNode) {
        this.initialNode = initialNode;
    }

    @Override
    public List<N> solve(Collection<? extends N> nodes) {
        List<N> result = solveNearestNeighbours(nodes);
        result = applyHeuristics(result);
        return result;
    }

    public List<N> solveNearestNeighbours(Collection<? extends N> nodes) {
        List<N> result = new ArrayList<N>(nodes.size());
        if (nodes.isEmpty()) return result;
        List<N> available = new LinkedList<N>(nodes);
        N previous;
        if (initialNode == null) {
            previous = available.remove(0);
            result.add(previous);
        } else {
            previous = initialNode;
        }
        while (!available.isEmpty()) {
            double bestDistance = Double.POSITIVE_INFINITY;
            N bestNode = null;
            for (N node : available) {
                double distance = previous.distance(node);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestNode = node;
                }
            }
            available.remove(bestNode);
            result.add(bestNode);
            previous = bestNode;
        }
        return result;
    }

    public List<N> applyHeuristics(Collection<? extends N> nodes) {
        List<N> result = new ArrayList<N>(nodes);
        int iter = 0;
        while (iter < maxIterations) {
            boolean found = false;
            for (; iter < maxIterations; iter++) {
                List<N> move = findOneMove(result);
                if (move == null)
                    break;
                result = move;
                found = true;
            }
            for (; iter < maxIterations; iter++) {
                List<N> swap = find2Opt(result);
                if (swap == null)
                    break;
                result = swap;
                found = true;
            }
            if (!found)
                break;
        }
        return result;
    }

    private double totalDistance(List<? extends N> nodes) {
        if (nodes.isEmpty()) return 0.0;
        double result = initialNode == null ? 0.0 : initialNode.distance(nodes.get(0));
        N previous = null;
        for (N current : nodes) {
            if (previous != null)
                result += previous.distance(current);
            previous = current;
        }
        return result;
    }

    private List<N> moveOne(List<? extends N> nodes, int source, int destination) {
        List<N> result = new ArrayList<N>(nodes);
        N node = result.remove(source);
        result.add(destination, node);
        return result;
    }

    private List<N> findOneMove(List<? extends N> nodes) {
        double minLength = totalDistance(nodes);
        List<N> result = null;
        for (int source = 0, sz = nodes.size(); source < sz; source++) {
            for (int destination = 0; destination < sz; destination++) {
                if (source == destination) continue;
                List<N> move = moveOne(nodes, source, destination);
                double length = totalDistance(move);
                if (length < minLength) {
                    result = move;
                    minLength = length;
                }
            }
        }
        return result;
    }

    private List<N> swap2Opt(List<? extends N> nodes, int m, int n) {
        int sz = nodes.size();
        List<N> result = new ArrayList<N>(sz);
        for (int i = 0; i < m; i++)
            result.add(nodes.get(i));
        for (int i = n; i >= m; i--)
            result.add(nodes.get(i));
        for (int i = n + 1; i < sz; i++)
            result.add(nodes.get(i));
        return result;
    }

    private List<N> find2Opt(List<? extends N> nodes) {
        final double curLength = totalDistance(nodes);
        double minLength = curLength;
        List<N> result = null;
        for (int m = 0, sz = nodes.size(); m < sz; m++) {
            for (int n = m + 1; n < sz; n++) {
                List<N> swap = swap2Opt(nodes, m, n);
                double length = totalDistance(swap);
                if (length < minLength) {
                    result = swap;
                    minLength = length;
                }
            }
        }
        return result;
    }
}
