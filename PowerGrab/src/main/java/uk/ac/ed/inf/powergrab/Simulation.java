/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a simulation of a given drone.
 */
public class Simulation {
    /**
     * Stores details about a drone's move.
     */
    public static class Move {
        public final Position before, after;
        public final Direction direction;
        public final double coins, power;

        public Move(Position before, Position after, Direction direction, double coins, double power) {
            this.before = before;
            this.after = after;
            this.direction = direction;
            this.coins = coins;
            this.power = power;
        }

        @Override
        public String toString() {
            return before.toString() + "," + direction.name() + "," + after.toString() + "," + coins + "," + power;
        }
    }

    private final Drone drone;
    private final int maxMoves;

    /**
     * Creates a simulation.
     *
     * @param drone the drone to simulate
     * @param maxMoves the maximum number of moves the drone is allowed to make
     */
    public Simulation(Drone drone, int maxMoves) {
        if (drone == null || maxMoves < 0) throw new IllegalArgumentException();
        this.drone = drone;
        this.maxMoves = maxMoves;
    }

    /**
     * Runs a simulation and returns a list of moves the drone made.
     *
     * @return a list of moves
     */
    public List<Move> runSimulation() {
        List<Move> result = new ArrayList<>(maxMoves);
        for (int moves = 0; moves < maxMoves && drone.getPower() >= GameRules.powerConsumedPerMove; moves++) {
            Position before, after;
            before = drone.getPosition();
            Direction direction = drone.getDirection();
            drone.move(direction);
            after = drone.getPosition();
            Move move = new Move(before, after, direction, drone.getCoins(), drone.getPower());
            result.add(move);
        }
        return result;
    }
}
