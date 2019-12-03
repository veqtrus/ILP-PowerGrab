package uk.ac.ed.inf.powergrab.search;

import java.util.Collection;
import java.util.List;

/**
 * Interface for a Travelling Salesman Problem solver
 *
 * @see Solver
 * @param <N> the type of nodes
 */
public interface TspSolver<N> extends Solver<Collection<? extends N>, List<N>> {
    /**
     * An interface nodes in a TSP may be required to implement
     * to provide the distance/cost between nodes.
     *
     * @param <T> the type of node
     */
    interface Node<T extends Node<T>> {
        /**
         * Returns the distance/cost between this node and {@code other}.
         *
         * @return distance/cost
         */
        double distance(T other);
    }

    /**
     * Returns the initial node if it has been set, {@code null} otherwise.
     *
     * @return the initial node or {@code null}
     */
    N getInitialNode();

    /**
     * Sets the fixed initial node. Set to {@code null} to disable the restriction.
     */
    void setInitialNode(N initialNode);

    /**
     * Finds the shortest tour of the given {@code nodes}.
     *
     * @see #setInitialNode
     * @param nodes the nodes to visit, excluding the fixed initial node
     * @return shortest tour, excluding the fixed initial node
     */
    List<N> solve(Collection<? extends N> nodes);
}
