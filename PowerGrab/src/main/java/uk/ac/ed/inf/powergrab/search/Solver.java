package uk.ac.ed.inf.powergrab.search;

/**
 * Interface for a problem solver
 *
 * @param <P> the type of the problem
 * @param <S> the type of the solution
 */
public interface Solver<P, S> {
    /**
     * Returns a solution to the {@code problem}.
     */
    S solve(P problem);
}
