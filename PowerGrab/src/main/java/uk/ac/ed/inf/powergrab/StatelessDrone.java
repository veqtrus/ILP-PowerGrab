package uk.ac.ed.inf.powergrab;

import java.util.*;

public class StatelessDrone extends Drone {
    public StatelessDrone(Position position, GameMap map, double coins, double power) {
        super(position, map, coins, power);
    }

    private static class ReachableStationsComparator implements Comparator<Direction> {
        private final Map<Direction, Station> reachableStations;

        public ReachableStationsComparator(Map<Direction, Station> reachableStations) {
            this.reachableStations = reachableStations;
        }

        public int compare(Direction d1, Direction d2) {
            Station s1 = reachableStations.get(d1), s2 = reachableStations.get(d2);
            double score1 = 0.0, score2 = 0.0;
            if (s1 != null)
                score1 = score(s1);
            if (s2 != null)
                score2 = score(s2);
            return Double.compare(score1, score2);
        }

        private static double score(Station station) {
            return station.getCoins() + station.getPower();
        }
    }

    public Direction getDirection() {
        Map<Direction, Station> reachableStations = new HashMap<Direction, Station>();
        ArrayList<Direction> dirs = new ArrayList<Direction>();
        for (Direction dir : Direction.values()) {
            Position next = getPosition().nextPosition(dir);
            if (next.inPlayArea()) {
                dirs.add(dir);
                Station nearestStation = map.nearestStation(next);
                reachableStations.put(dir, nearestStation.position.isClose(next) ? nearestStation : null);
            }
        }
        Collections.shuffle(dirs, random);
        return Collections.max(dirs, new ReachableStationsComparator(reachableStations));
    }
}
