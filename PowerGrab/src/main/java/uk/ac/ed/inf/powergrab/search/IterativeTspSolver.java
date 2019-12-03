package uk.ac.ed.inf.powergrab.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@linkplain TspSolver Travelling Salesman Problem solver} which finds a
 * solution using the <em>Nearest Neighbours</em> and <em>3-opt</em> heuristics.
 *
 * @see TspSolver
 * @param <N> the type of nodes
 */
public class IterativeTspSolver<N extends TspSolver.Node<N>> implements TspSolver<N> {
    private int maxIterations;
    private N initialNode = null;
    private boolean symmetric = true;

    /**
     * Creates a {@code IterativeTspSolver} instance without a limit on iterations.
     */
    public IterativeTspSolver() {
        maxIterations = Integer.MAX_VALUE;
    }

    /**
     * Creates a {@code IterativeTspSolver} instance with a limit on iterations.
     */
    public IterativeTspSolver(int maxIterations) {
        setMaxIterations(maxIterations);
    }

    public int getMaxIterations() { return maxIterations; }

    /**
     * Sets the maximum number of iterations.
     *
     * @throws IllegalArgumentException if {@code maxIterations} is negative
     */
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

    public boolean getSymmetric() { return symmetric; }

    /**
     * Sets whether the TSP is symmetric, {@code true} by default.
     *
     * <p>In a symmetric TSP for all nodes {@code a} and {@code b},
     * {@code a.distance(b) == b.distance(a)}.
     */
    public void setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
    }

    /**
     * First constructs a solution using the {@linkplain #solveNearestNeighbours Nearest Neighbours heuristic}
     * and then iteratively applies the {@linkplain #applyHeuristics 3-opt heuristic}.
     *
     * @see #setInitialNode
     * @param nodes the nodes to visit, excluding the fixed initial node
     * @return shortest tour, excluding the fixed initial node
     */
    @Override
    public List<N> solve(Collection<? extends N> nodes) {
        List<N> result = solveNearestNeighbours(nodes);
        result = applyHeuristics(result);
        return result;
    }

    /**
     * Returns a solution found using the <em>Nearest Neighbours</em> heuristic.
     */
    public List<N> solveNearestNeighbours(Collection<? extends N> nodes) {
        ArrayList<N> result = new ArrayList<>(nodes.size());
        if (nodes.isEmpty()) return result;
        ArrayList<N> available = new ArrayList<>(nodes);
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

    /**
     * Returns a solution found by iteratively applying the <em>3-opt</em> heuristic.
     */
    public List<N> applyHeuristics(Collection<? extends N> nodes) {
        ArrayList<N> result = new ArrayList<>(nodes);
        if (result.size() < 2) return result;
        for (int i = 0; i < maxIterations; i++)
            if (!threeOpt(result))
                break;
        return result;
    }

    /**
     * Applies the <em>3-opt</em> heuristic once.
     *
     * <p>Adapted from the pseudocode in
     * <a href="https://en.wikipedia.org/wiki/3-opt" target="_top">Wikipedia: 3-opt</a>.
     *
     * @return {@code true} if {@code nodes} has been modified
     */
    private boolean threeOpt(ArrayList<N> nodes) {
        ArrayList<N> keyNodes = new ArrayList<>(6);
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
                                    // if the TSP is symmetric we can calculate the distance delta
                                    // by considering just 6 key nodes
                                    addKeyNodes(keyNodes, nodes, i, j, k);
                                    before = keyNodesDistance(keyNodes);
                                    rearrange(keyNodes, 1, 3, 5, swap, revA, revB);
                                    after = keyNodesDistance(keyNodes);
                                } else {
                                    // if the TSP is not symmetric we have to calculate the distance
                                    // delta from the whole rearrangement
                                    ArrayList<N> rearrangement = new ArrayList<>(nodes);
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
            // found an improvement so rearrange the whole tour
            rearrange(nodes, bestI, bestJ, bestK, bestSwap, bestRevA, bestRevB);
            return true;
        }
        return false;
    }

    /**
     * Extracts the key nodes that will affect the total distance of the tour
     * based on {@code i, j, k}.
     */
    private void addKeyNodes(ArrayList<N> keyNodes, ArrayList<N> nodes, int i, int j, int k) {
        keyNodes.clear();
        keyNodes.add(i == 0 ? initialNode : nodes.get(i - 1));
        keyNodes.add(nodes.get(i));
        keyNodes.add(nodes.get(j - 1));
        keyNodes.add(nodes.get(j));
        keyNodes.add(nodes.get(k - 1));
        keyNodes.add(k == nodes.size() ? null : nodes.get(k));
    }

    /**
     * Partitions the tour according to {@code i, j, k} and reconnects it
     * according to {@code swap, revA, revB}.
     */
    private void rearrange(ArrayList<N> nodes, int i, int j, int k, boolean swap, boolean revA, boolean revB) {
        if (swap) {
            // To swap the two halves of PQRS we can
            //  1. Reverse all of it (SRQP)
            //  2. Reverse the first half (RSQP)
            //  3. Reverse the second half (RSPQ)
            Collections.reverse(nodes.subList(i, k));
            j = k - j + i; // if the two parts have different sizes we need to update the midpoint
            revA = !revA;
            revB = !revB;
        }
        if (revA) Collections.reverse(nodes.subList(i, j));
        if (revB) Collections.reverse(nodes.subList(j, k));
    }

    /**
     * Returns the distance between the key nodes that will affect the total distance.
     */
    private double keyNodesDistance(ArrayList<N> keyNodes) {
        double result = 0.0;
        for (int i = 0; i < 6; i += 2) {
            N a = keyNodes.get(i), b = keyNodes.get(i + 1);
            if (a != null && b != null)
                result += a.distance(b);
        }
        return result;
    }

    /**
     * Returns the total distance of the given tour.
     */
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
