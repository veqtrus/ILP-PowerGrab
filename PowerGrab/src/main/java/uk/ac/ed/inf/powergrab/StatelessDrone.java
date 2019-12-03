/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

import java.util.*;

/**
 * A {@link Drone} implementation which is memoryless, with limited look-ahead.
 */
public class StatelessDrone extends Drone {
    /**
     * Creates a {@code StatelessDrone} instance.
     */
    public StatelessDrone(Position position, GameMap map, double coins, double power) {
        super(position, map, coins, power);
    }

    /**
     * Implements a {@link Comparator} which compares {@link Direction}s
     * based on the coins than will be transferred after the move.
     */
    private static class ReachableStationsComparator implements Comparator<Direction> {
        private final Map<Direction, Station> reachableStations;

        public ReachableStationsComparator(Map<Direction, Station> reachableStations) {
            this.reachableStations = reachableStations;
        }

        public int compare(Direction d1, Direction d2) {
            Station s1 = reachableStations.get(d1), s2 = reachableStations.get(d2);
            double coins1 = 0.0, coins2 = 0.0;
            if (s1 != null)
                coins1 = s1.getCoins();
            if (s2 != null)
                coins2 = s2.getCoins();
            return Double.compare(coins1, coins2);
        }
    }

    /**
     * Returns the best direction according to the amount of coins
     * that can be transferred, or a random one in the case of a tie.
     *
     * @return the direction this drone chose to move next
     */
    public Direction getDirection() {
        HashMap<Direction, Station> reachableStations = new HashMap<>();
        ArrayList<Direction> dirs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            Position next = getPosition().nextPosition(dir);
            if (next.inPlayArea()) {
                reachableStations.put(dir, map.closeStation(next));
                dirs.add(dir);
            }
        }
        Collections.shuffle(dirs, random);
        return Collections.max(dirs, new ReachableStationsComparator(reachableStations));
    }
}
