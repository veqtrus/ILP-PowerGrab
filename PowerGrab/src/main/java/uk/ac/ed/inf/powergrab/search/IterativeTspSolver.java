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
        int iter = 0;
        for (; iter < maxIterations; iter++)
            if (!threeOpt(result))
                break;
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

    private boolean threeOpt(ArrayList<N> nodes) {
        ArrayList<N> keyNodes = new ArrayList<N>(6);
        int bestI = 0, bestJ = 0, bestK = 0, bestRevAB = 0, bestRevA = 0, bestRevB = 0;
        double bestDelta = 0.0;
        for (int i = 0, sz = nodes.size(); i < sz; i++) {
            for (int j = i + 1; j < sz; j++) {
                for (int k = j + 1; k <= sz; k++) {
                    for (int revAB = 0; revAB < 2; revAB++) {
                        for (int revA = 0; revA < 2; revA++) {
                            for (int revB = 0; revB < 2; revB++) {
                                if (revAB + revA + revB == 0) continue;
                                addKeyNodes(keyNodes, nodes, i, j, k);
                                double before = keyNodesDistance(keyNodes);
                                rearrange(keyNodes, 1, 3, 5, revAB, revA, revB);
                                double after = keyNodesDistance(keyNodes);
                                double delta = after - before;
                                if (delta < bestDelta) {
                                    bestDelta = delta;
                                    bestI = i;
                                    bestJ = j;
                                    bestK = k;
                                    bestRevAB = revAB;
                                    bestRevA = revA;
                                    bestRevB = revB;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (bestDelta < 0.0) {
            rearrange(nodes, bestI, bestJ, bestK, bestRevAB, bestRevA, bestRevB);
            return true;
        }
        return false;
    }

    private void addKeyNodes(ArrayList<N> keyNodes, ArrayList<N> nodes, int i, int j, int k) {
        keyNodes.clear();
        keyNodes.add(i == 0 ? initialNode : nodes.get(i - 1));
        keyNodes.add(nodes.get(i));
        keyNodes.add(nodes.get(j - 1));
        keyNodes.add(nodes.get(j));
        keyNodes.add(nodes.get(k - 1));
        keyNodes.add(k == nodes.size() ? null : nodes.get(k));
    }

    private void rearrange(ArrayList<N> nodes, int i, int j, int k, int revAB, int revA, int revB) {
        if (revAB != 0) Collections.reverse(nodes.subList(i, k));
        if (revA != 0) Collections.reverse(nodes.subList(i, j));
        if (revB != 0) Collections.reverse(nodes.subList(j, k));
    }

    private double keyNodesDistance(ArrayList<N> keyNodes) {
        double result = 0.0;
        for (int i = 0; i < 6; i += 2) {
            N a = keyNodes.get(i), b = keyNodes.get(i + 1);
            if (a != null && b != null)
                result += a.distance(b);
        }
        return result;
    }
}
