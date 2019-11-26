package uk.ac.ed.inf.powergrab.search;

import java.util.Collection;
import java.util.List;

public interface TspSolver<N> {
    interface Node<T extends Node<T>> {
        double distance(T other);
    }

    N getInitialNode();

    void setInitialNode(N initialNode);

    List<N> solve(Collection<? extends N> nodes);
}
