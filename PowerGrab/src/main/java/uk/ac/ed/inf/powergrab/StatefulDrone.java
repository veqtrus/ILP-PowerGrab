package uk.ac.ed.inf.powergrab;

import uk.ac.ed.inf.powergrab.search.*;

import java.util.*;

public class StatefulDrone extends Drone {
    private final int maxMoves;
    private final Queue<Direction> moves;

    public StatefulDrone(Position position, GameMap map, double coins, double power, int maxMoves) {
        super(position, map, coins, power);
        this.maxMoves = maxMoves;
        this.moves = new ArrayDeque<Direction>(maxMoves);
    }

    @Override
    public void move(Direction direction) {
        if (!moves.isEmpty() && !moves.poll().equals(direction))
            moves.clear();
        super.move(direction);
    }

    public Direction getDirection() {
        if (!moves.isEmpty())
            return moves.peek();
        List<Position> strategy = new ArrayList<Position>(map.stations.size());
        for (Station station : map.stations) {
            if (station.getCoins() > 0.0)
                strategy.add(station.position);
        }
        TspSolver<Position> tspSolver = new IterativeTspSolver<Position>();
        tspSolver.setInitialNode(getPosition());
        strategy = tspSolver.solve(strategy);
        HeuristicSearch<PathNode> heuristicSearch = new HeuristicSearch<PathNode>(4096);
        PathNode initPathNode = new PathNode(0, getPosition(), getCoins(), getPower(), map, strategy);
        PathNode solution = heuristicSearch.search(initPathNode);
        if (solution != null)
            moves.addAll(solution.getDirections());
        if (!moves.isEmpty())
            return moves.peek();
        return awayFromNegativity();
    }

    private Direction awayFromNegativity() {
        double bestScore = Double.POSITIVE_INFINITY;
        Direction result = null;
        Position position = getPosition();
        for (Direction direction : Direction.values()) {
            Position next = position.nextPosition(direction);
            if (!next.inPlayArea()) continue;
            double score = 0.0;
            Station nearestStation = map.nearestStation(next);
            for (Station station : map.stations) {
                double multiplier = 0.0;
                double distance = station.position.distance(next);
                if (station.getCoins() < 0.0 || station.getPower() < 0.0)
                    multiplier = distance < GameRules.closeDistance && nearestStation.equals(station) ? 1e10 : 1.0;
                score += multiplier / (1e-10 + distance);
            }
            if (score < bestScore) {
                bestScore = score;
                result = direction;
            }
        }
        return result;
    }

    private final class PathNode implements HeuristicSearch.Node<PathNode> {
        final int move;
        final PathNode previous;
        final Direction direction;
        final Position position;
        double coins, power, distance, coinsLost;
        GameMap map;
        List<Position> plan;

        PathNode(int move, Position position, double coins, double power, GameMap map, List<Position> plan) {
            this.move = move;
            this.previous = null;
            this.direction = null;
            this.position = position;
            this.coins = coins;
            this.power = power;
            this.coinsLost = this.distance = 0.0;
            this.map = map;
            this.plan = plan;
        }

        PathNode(PathNode previous, Direction direction) {
            this.move = previous.move + 1;
            this.previous = previous;
            this.direction = direction;
            this.position = previous.position.nextPosition(direction);
            this.coins = previous.coins;
            this.power = previous.power - GameRules.powerConsumedPerMove;
            this.coinsLost = previous.coinsLost;
            this.distance = previous.distance + GameRules.moveDistance;
            this.map = previous.map;
            this.plan = previous.plan;
        }

        List<Direction> getDirections() {
            LinkedList<Direction> result = new LinkedList<Direction>();
            for (PathNode node = this; node != null; node = node.previous)
                if (node.direction != null)
                    result.addFirst(node.direction);
            return result;
        }

        @Override
        public Iterable<PathNode> childNodes() {
            ArrayList<PathNode> result = new ArrayList<PathNode>(16);
            if (power < GameRules.powerConsumedPerMove)
                return result;
            for (Direction direction : Direction.values()) {
                PathNode next = new PathNode(this, direction);
                if (!next.position.inPlayArea()) continue;
                Drone drone = new StatefulDrone(position, new GameMap(map), coins, power, maxMoves - move);
                drone.move(direction);
                Position dronePosition = drone.getPosition();
                Station nearestStation = map.nearestStation(dronePosition);
                if (nearestStation.position.isClose(dronePosition)) {
                    next.map = drone.map;
                    if (plan.contains(nearestStation.position)) {
                        next.plan = new ArrayList<Position>(plan);
                        next.plan.remove(nearestStation.position);
                    }
                }
                if (plan.contains(dronePosition)) {
                    next.plan = new ArrayList<Position>(plan);
                    next.plan.remove(dronePosition);
                }
                next.coins = drone.getCoins();
                next.power = drone.getPower();
                double deltaCoins = next.coins - coins;
                if (deltaCoins < 0.0)
                    next.coinsLost -= deltaCoins;
                result.add(next);
            }
            return result;
        }

        @Override
        public boolean isGoal() {
            return plan.isEmpty() || move >= maxMoves || power < GameRules.powerConsumedPerMove;
        }

        @Override
        public int compareTo(PathNode other) {
            int compPlanSize = plan.size() - other.plan.size();
            if (compPlanSize != 0)
                return compPlanSize;
            int compCoinsLost = Double.compare(coinsLost, other.coinsLost);
            if (compCoinsLost != 0)
                return compCoinsLost;
            return Double.compare(expectedDistance(), other.expectedDistance());
        }

        private double expectedDistance() {
            if (plan.isEmpty())
                return distance;
            Position target = plan.get(0);
            return distance + position.distance(target);
        }

        @Override
        public boolean equivalent(PathNode o) {
            return o.position.equals(position);
        }
    }
}
