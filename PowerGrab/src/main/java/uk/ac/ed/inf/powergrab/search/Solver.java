/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

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
