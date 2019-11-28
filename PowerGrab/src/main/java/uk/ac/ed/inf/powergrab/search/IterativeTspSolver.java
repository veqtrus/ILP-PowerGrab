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
        for (int iter = 0; iter < maxIterations; iter++) {
            List<N> swap = find3Opt(result);
            if (swap == null) break;
            result = swap;
        }
        return result;
    }

    private double totalDistance(List<? extends N> nodes) {
        if (nodes.isEmpty()) return 0.0;
        double result = 0.0;
        N previous = initialNode;
        for (N current : nodes) {
            if (previous != null)
                result += previous.distance(current);
            previous = current;
        }
        return result;
    }

    private List<N> find3Opt(List<? extends N> nodes) {
        double minLength = totalDistance(nodes);
        List<N> result = null;
        for (int i = 0, sz = nodes.size(); i < sz; i++) {
            for (int j = i + 1; j < sz; j++) {
                for (int k = j + 1; k <= sz; k++) {
                    for (int swapAB = 0; swapAB < 2; swapAB++) {
                        for (int swapA = 0; swapA < 2; swapA++) {
                            for (int swapB = 0; swapB < 2; swapB++) {
                                if (swapAB + swapA + swapB == 0) continue;
                                List<N> swap = new ArrayList<N>(nodes);
                                if (swapAB != 0) Collections.reverse(swap.subList(i, k));
                                if (swapA != 0) Collections.reverse(swap.subList(i, j));
                                if (swapB != 0) Collections.reverse(swap.subList(j, k));
                                double length = totalDistance(swap);
                                if (length < minLength) {
                                    result = swap;
                                    minLength = length;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
