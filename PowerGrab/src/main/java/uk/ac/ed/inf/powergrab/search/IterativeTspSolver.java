package uk.ac.ed.inf.powergrab.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IterativeTspSolver<N extends TspSolver.Node<N>> implements TspSolver<N> {
    private int maxIterations;
    private N initialNode;

    public boolean symmetric = true;

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
            if (bestNode == null) break;
            available.remove(bestNode);
            result.add(bestNode);
            previous = bestNode;
        }
        return result;
    }

    public List<N> applyHeuristics(Collection<? extends N> nodes) {
        ArrayList<N> result = new ArrayList<N>(nodes);
        if (result.size() < 2) return result;
        for (int i = 0; i < maxIterations; i++)
            if (!threeOpt(result))
                break;
        return result;
    }

    private boolean threeOpt(ArrayList<N> nodes) {
        ArrayList<N> keyNodes = new ArrayList<N>(6);
        int bestI = 0, bestJ = 0, bestK = 0;
        boolean bestSwap = false, bestRevA = false, bestRevB = false;
        double bestDelta = 0.0, currentTotalDistance = 0.0;
        if (!symmetric)
            currentTotalDistance = totalDistance(nodes);
        for (int i = 0, sz = nodes.size(); i < sz; i++) {
            for (int j = i + 1; j < sz; j++) {
                for (int k = j + 1; k <= sz; k++) {
                    for (int iSwap = 0; iSwap < 2; iSwap++) {
                        for (int iRevA = 0; iRevA < 2; iRevA++) {
                            for (int iRevB = 0; iRevB < 2; iRevB++) {
                                if (iSwap + iRevA + iRevB == 0) continue;
                                boolean swap = iSwap != 0, revA = iRevA != 0, revB = iRevB != 0;
                                double before, after;
                                if (symmetric) {
                                    addKeyNodes(keyNodes, nodes, i, j, k);
                                    before = keyNodesDistance(keyNodes);
                                    rearrange(keyNodes, 1, 3, 5, swap, revA, revB);
                                    after = keyNodesDistance(keyNodes);
                                } else {
                                    ArrayList<N> rearrangement = new ArrayList<N>(nodes);
                                    rearrange(rearrangement, i, j, k, swap, revA, revB);
                                    after = totalDistance(rearrangement);
                                    before = currentTotalDistance;
                                }
                                double delta = after - before;
                                if (delta < bestDelta) {
                                    bestDelta = delta;
                                    bestI = i;
                                    bestJ = j;
                                    bestK = k;
                                    bestSwap = swap;
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
            rearrange(nodes, bestI, bestJ, bestK, bestSwap, bestRevA, bestRevB);
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

    private void rearrange(ArrayList<N> nodes, int i, int j, int k, boolean swap, boolean revA, boolean revB) {
        if (swap) {
            Collections.reverse(nodes.subList(i, k));
            j = k - j + i;
            revA = !revA;
            revB = !revB;
        }
        if (revA) Collections.reverse(nodes.subList(i, j));
        if (revB) Collections.reverse(nodes.subList(j, k));
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

    private double totalDistance(ArrayList<N> nodes) {
        double result = 0.0;
        N previous = initialNode;
        for (N current : nodes) {
            if (previous != null)
                result += previous.distance(current);
            previous = current;
        }
        return result;
    }
}
