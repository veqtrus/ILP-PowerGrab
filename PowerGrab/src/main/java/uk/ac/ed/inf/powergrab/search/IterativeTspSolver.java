package uk.ac.ed.inf.powergrab.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        ArrayList<N> result = new ArrayList<N>(nodes.size());
        if (nodes.isEmpty()) return result;
        ArrayList<N> available = new ArrayList<N>(nodes);
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
        ArrayList<N> result = new ArrayList<N>(nodes);
        if (result.size() < 2) return result;
        for (int iter = 0; iter < maxIterations; iter++) {
            ArrayList<N> rearrangement = find3Opt(result);
            if (rearrangement == null)
                break;
            result = rearrangement;
        }
        return result;
    }

    private double totalDistance(ArrayList<N> nodes) {
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

    private ArrayList<N> find3Opt(ArrayList<N> nodes) {
        double minLength = totalDistance(nodes);
        ArrayList<N> result = null;
        for (int i = 0, sz = nodes.size(); i < sz; i++) {
            for (int j = i + 1; j < sz; j++) {
                for (int k = j + 1; k <= sz; k++) {
                    for (int swap = 0; swap < 2; swap++) {
                        for (int revA = 0; revA < 2; revA++) {
                            for (int revB = 0; revB < 2; revB++) {
                                if (swap + revA + revB == 0) continue;
                                ArrayList<N> rearrangement = rearrange(nodes, i, j, k, swap != 0, revA != 0, revB != 0);
                                double length = totalDistance(rearrangement);
                                if (length < minLength) {
                                    result = rearrangement;
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

    private ArrayList<N> rearrange(ArrayList<N> nodes, int i, int j, int k, boolean swap, boolean revA, boolean revB) {
        ArrayList<N> rearrangement = new ArrayList<N>(nodes);
        if (swap) {
            Collections.reverse(rearrangement.subList(i, k));
            j = k - j + i;
            revA = !revA;
            revB = !revB;
        }
        if (revA) Collections.reverse(rearrangement.subList(i, j));
        if (revB) Collections.reverse(rearrangement.subList(j, k));
        return rearrangement;
    }
}
