package uk.ac.ed.inf.powergrab.search;

import org.paulgeorgiou.collections.TreePriorityDeque;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Implements a best-first search algorithm.
 * {@linkplain Node Nodes} have the responsibility of providing child nodes,
 * cost calculation, and goal checking.
 *
 * @param <N> the type of nodes
 */
public class HeuristicSearch<N extends HeuristicSearch.Node<N>> {
    /**
     * The interface all nodes have to implement for providing child nodes
     * and goal checking. Nodes also have to be {@link Comparable}, with
     * least nodes considered the best.
     *
     * @see Comparable
     * @param <T> the type of node
     */
    public interface Node<T extends Node<T>> extends Comparable<T> {
        /**
         * Returns a collection of nodes that can be visited from this node.
         *
         * @return collection of nodes
         */
        Collection<? extends T> childNodes();

        /**
         * Returns {@code true} if this node is a goal. Search finishes when
         * a goal node is visited.
         *
         * @return {@code true} if this node is a goal
         */
        boolean isGoal();

        /**
         * Returns {@code true} if this node is equivalent to the parameter
         * {@code p}. If an equivalent node has already been explored, this
         * node will only be explored if it has lower cost.
         *
         * @return {@code true} if this node is equivalent to the parameter
         */
        default boolean equivalent(T o) { return equals(o); }
    }

    private int maxExploredSize, maxFrontierSize;

    /**
     * Creates a {@code HeuristicSearch} instance with unbounded frontier
     * and explored sets.
     */
    public HeuristicSearch() {
        maxExploredSize = maxFrontierSize = Integer.MAX_VALUE;
    }

    /**
     * Creates a {@code HeuristicSearch} instance with frontier and explored
     * sets being bounded to {@code maxQueueSizes} elements.
     */
    public HeuristicSearch(int maxQueueSizes) {
        setMaxExploredSize(maxQueueSizes);
        setMaxFrontierSize(maxQueueSizes);
    }

    public int getMaxExploredSize() { return maxExploredSize; }

    /**
     * Sets the maximum size of the explored set.
     *
     * @throws IllegalArgumentException if {@code maxExploredSize} is negative
     */
    public void setMaxExploredSize(int maxExploredSize) {
        if (maxExploredSize < 0) throw new IllegalArgumentException();
        this.maxExploredSize = maxExploredSize;
    }

    /**
     * Sets the maximum size of the frontier set.
     *
     * @throws IllegalArgumentException if {@code maxFrontierSize} is less than one
     */
    public int getMaxFrontierSize() { return maxFrontierSize; }

    public void setMaxFrontierSize(int maxFrontierSize) {
        if (maxFrontierSize < 1) throw new IllegalArgumentException();
        this.maxFrontierSize = maxFrontierSize;
    }

    /**
     * Runs a heuristic search.
     *
     * <p>Adapted from the Uniform Cost Search algorithm in
     * Artificial Intelligence: A Modern Approach by Russel and Norvig.
     *
     * @param first the first node to search
     * @return the solution or {@code null} if not found
     */
    public N search(N first) {
        // for the explored set consider equivalent nodes to be equal
        TreeMap<N, N> explored = new TreeMap<N, N>((n1, n2) -> n1.equivalent(n2) ? 0 : n1.compareTo(n2));
        TreePriorityDeque<N> frontier = new TreePriorityDeque<N>();
        frontier.setMaxSize(maxFrontierSize); // limit the size of the frontier set
        frontier.add(first);
        while (!frontier.isEmpty()) {
            N node = frontier.removeFirst(); // get the current best node
            if (node.isGoal()) return node;
            // if a lower cost node has been found it will replace
            // the existing one in the explored set
            explored.put(node, node);
            // limit the size of the explored set
            while (explored.size() > maxExploredSize)
                explored.pollLastEntry();
            for (N child : node.childNodes()) {
                // get the equivalent node in the explored set if it exists
                N visited = explored.get(child);
                // add child to frontier if not already explored or it has lower cost
                if (visited == null || child.compareTo(visited) < 0)
                    frontier.addLast(child);
            }
        }
        return null; // no solution found
    }
}
