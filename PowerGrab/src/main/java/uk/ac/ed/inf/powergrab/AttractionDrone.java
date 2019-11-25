package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AttractionDrone extends Drone {
    private final int maxMemory = 10;
    private final List<Position> visited;

    public AttractionDrone(Position position, GameMap map, double coins, double power) {
        super(position, map, coins, power);
        visited = new LinkedList<Position>();
    }

    @Override
    public void move(Direction direction) {
        super.move(direction);
        visited.add(getPosition());
        if (visited.size() > maxMemory)
            visited.remove(0);
    }

    public Direction getDirection() {
        ArrayList<DirectionScore> dirs = new ArrayList<DirectionScore>(16);
        Position position = getPosition();
        for (Direction dir : Direction.values()) {
            Position next = position.nextPosition(dir);
            if (!next.inPlayArea()) continue;
            if (visited.contains(next)) continue;
            dirs.add(new DirectionScore(dir, score(next)));
        }
        return Collections.max(dirs).direction;
    }

    private double score(Position pos) {
        double result = 0.0;
        boolean onlyNegative = onlyNegative();
        for (Station station : map.stations()) {
            double coef = 0.0;
            double coins = station.getCoins();
            if (coins > 0.0) {
                coef = 1.0;
            } else if (coins < 0.0) {
                if (onlyNegative)
                    coef = station.position.isClose(pos) ? -1e10 : -1.0;
                else
                    coef = station.position.isClose(pos) ? -1.0 : 0.0;
            }
            result += coef / (1e-10 + station.position.distance(pos));
        }
        return result;
    }

    private boolean onlyNegative() {
        for (Station station : map.stations()) {
            if (station.getCoins() > 0.0)
                return false;
        }
        return true;
    }

    private static class DirectionScore implements Comparable<DirectionScore> {
        public final Direction direction;
        public final double score;

        public DirectionScore(Direction direction, double score) {
            this.direction = direction;
            this.score = score;
        }

        public int compareTo(DirectionScore o) {
            return Double.compare(score, o.score);
        }
    }
}
