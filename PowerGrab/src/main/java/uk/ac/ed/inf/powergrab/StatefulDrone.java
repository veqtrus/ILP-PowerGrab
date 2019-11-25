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
        List<StrategyNode> strategy = new ArrayList<StrategyNode>(map.stations().size());
        for (Station station : map.stations()) {
            if (station.getCoins() > 0.0)
                strategy.add(new StrategyNode(station));
        }
        TspSolver<StrategyNode> tspSolver = new TspSolver<StrategyNode>();
        tspSolver.setInitialNode(new StrategyNode(getPosition()));
        strategy = tspSolver.solve(strategy);
        List<String> plan = new ArrayList<String>(strategy.size());
        for (StrategyNode node : strategy)
            plan.add(node.station);
        HeuristicSearch<PathNode> heuristicSearch = new HeuristicSearch<PathNode>();
        PathNode initPathNode = new PathNode(0, getCoins(), getPower(), 0.0, map, getPosition(), null, plan, null);
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
            for (Station station : map.stations()) {
                double multiplier = 0.0;
                double distance = station.position.distance(next);
                if (station.getCoins() < 0.0 || station.getPower() < 0.0)
                    multiplier = distance < GameRules.closeDistance ? 1e10 : 1.0;
                score += multiplier / (1e-10 + distance);
            }
            if (score < bestScore) {
                bestScore = score;
                result = direction;
            }
        }
        return result;
    }

    private final class StrategyNode implements TspSolver.Node<StrategyNode> {
        public final String station;
        public final Position position;

        public StrategyNode(Station station) {
            this.station = station.id;
            this.position = station.position;
        }

        public StrategyNode(Position position) {
            this.station = null;
            this.position = position;
        }

        @Override
        public double distance(StrategyNode other) {
            return other.position.distance(position);
        }
    }

    private final class PathNode implements HeuristicSearch.Node<PathNode> {
        public final int move;
        public final double coins, power, distance;
        public final GameMap map;
        public final Position position;
        public final Direction direction;
        public final List<String> plan;
        public final PathNode previous;

        public PathNode(int move, double coins, double power, double distance, GameMap map, Position position, Direction direction,
            List<String> plan, PathNode previous) {
            this.move = move;
            this.coins = coins;
            this.power = power;
            this.distance = distance;
            this.map = new GameMap(map);
            this.position = position;
            this.direction = direction;
            this.plan = plan;
            this.previous = previous;
        }

        public List<Direction> getDirections() {
            LinkedList<Direction> result = new LinkedList<Direction>();
            for (PathNode node = this; node != null; node = node.previous)
                if (node.direction != null)
                    result.addFirst(node.direction);
            return result;
        }

        @Override
        public Collection<PathNode> childNodes() {
            ArrayList<PathNode> result = new ArrayList<PathNode>(16);
            if (power < GameRules.powerConsumedPerMove)
                return result;
            for (Direction direction : Direction.values()) {
                Position nextPosition = position.nextPosition(direction);
                if (!nextPosition.inPlayArea()) continue;
                Drone drone = new StatefulDrone(position, new GameMap(map), coins, power, maxMoves - move);
                drone.move(direction);
                List<String> planForward = plan;
                Station nearestStation = map.nearestStation(drone.getPosition());
                if (nearestStation.position.isClose(drone.getPosition()) && planForward.contains(nearestStation.id)) {
                    planForward = new ArrayList<String>(plan);
                    planForward.remove(nearestStation.id);
                }
                result.add(new PathNode(
                    move + 1, drone.getCoins(), drone.getPower(), distance + GameRules.moveDistance,
                    drone.map, drone.getPosition(), direction, planForward, this));
            }
            return result;
        }

        @Override
        public boolean isGoal() {
            return plan.isEmpty() || move >= maxMoves || power < GameRules.powerConsumedPerMove;
        }

        @Override
        public int compareTo(PathNode n2) {
            final PathNode n1 = this;
            int compStationsRemaining = n1.plan.size() - n2.plan.size();
            if (compStationsRemaining != 0)
                return compStationsRemaining;
            double[] e1 = n1.expectedCoinsPowerDistance(), e2 = n2.expectedCoinsPowerDistance();
            int compExpCoins = -Double.compare(e1[0], e2[0]); // expected coins
            if (compExpCoins != 0)
                return compExpCoins;
            int compExpDistance = Double.compare(e1[2], e2[2]); // expected distance
            if (compExpDistance != 0)
                return compExpDistance;
            return -Double.compare(e1[1], e2[1]); // expected power
        }

        private double[] expectedCoinsPowerDistance() {
            if (plan.isEmpty())
                return new double[] { coins, power, distance };
            final double radius = GameRules.closeDistance;
            double expectedDistance = distance;
            expectedDistance += Math.max(position.distance(map.getStationById(plan.get(0)).position) - radius, 0.0);
            for (int i = 2, sz = plan.size(); i < sz; i++) {
                Station start = map.getStationById(plan.get(i - 1)), end = map.getStationById(plan.get(i));
                expectedDistance += Math.max(start.position.distance(end.position) - 2.0 * radius, 0.0);
            }
            double expectedCoins = coins;
            double expectedPower = power - expectedDistance * GameRules.powerConsumedPerMove / GameRules.moveDistance;
            for (Station station : map.stations()) {
                double coins = station.getCoins(), power = station.getPower();
                if (coins > 0.0) expectedCoins += coins;
                if (power > 0.0) expectedPower += power;
            }
            return new double[] { expectedCoins, expectedPower, expectedDistance };
        }

        @Override
        public boolean equivalent(PathNode o) {
            return o.position.equals(position);
        }
    }
}
