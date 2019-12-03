package uk.ac.ed.inf.powergrab;

import uk.ac.ed.inf.powergrab.search.*;

import java.util.*;

/**
 * A {@link Drone} implementation which decides its path by finding:
 * <ol>
 *  <li>the best order to visit stations;</li>
 *  <li>the shortest path to visit these stations.</li>
 * </ol>
 */
public class StatefulDrone extends Drone {
    private final int maxMoves;
    private final Queue<Direction> moves;

    /**
     * Creates a {@code StatefulDrone} instance.
     */
    public StatefulDrone(Position position, GameMap map, double coins, double power, int maxMoves) {
        super(position, map, coins, power);
        this.maxMoves = maxMoves;
        this.moves = new ArrayDeque<>(maxMoves);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Updates the queue of precomputed moves.
     */
    @Override
    public void move(Direction direction) {
        if (!moves.isEmpty() && !moves.poll().equals(direction))
            moves.clear();
        super.move(direction);
    }

    /**
     * Returns the first move from the queue of precomputed moves,
     * otherwise populates the queue.
     *
     * @return the direction this drone chose to move next
     */
    public Direction getDirection() {
        if (!moves.isEmpty())
            return moves.peek();
        List<Position> strategy = new ArrayList<>(map.stations.size());
        for (Station station : map.stations) {
            if (station.getCoins() > 0.0)
                strategy.add(station.position);
        }
        if (!strategy.isEmpty()) {
            TspSolver<Position> strategyFinder = new IterativeTspSolver<>(strategy.size());
            strategyFinder.setInitialNode(getPosition());
            strategy = strategyFinder.solve(strategy);
            Solver<PathNode, PathNode> pathFinder = new HeuristicSearchSolver<>(4096);
            PathNode initPathNode = new PathNode(0, getPosition(), getCoins(), getPower(), map, strategy);
            PathNode solution = pathFinder.solve(initPathNode);
            if (solution != null)
                moves.addAll(solution.getDirections());
        }
        if (!moves.isEmpty())
            return moves.peek();
        return awayFromNegativity();
    }

    /**
     * Returns the best direction away from negative stations.
     *
     * @return the direction this drone chose to move next
     */
    private Direction awayFromNegativity() {
        double bestScore = Double.POSITIVE_INFINITY;
        Direction result = null;
        for (Direction direction : Direction.values()) {
            Position next = getPosition().nextPosition(direction);
            if (!next.inPlayArea()) continue;
            double score = 0.0;
            Station closeStation = map.closeStation(next);
            for (Station station : map.stations) {
                double weight = 0.0;
                double distance = station.position.distance(next);
                // negative stations should be avoided, especially
                // those that would result in loss of coins or power
                if (station.getCoins() < 0.0 || station.getPower() < 0.0)
                    weight = station.equals(closeStation) ? 1e9 - station.getCoins() : 1.0;
                score += weight / (1e-9 + distance);
            }
            // minimising the score maximises the distances from negative stations
            if (score < bestScore) {
                bestScore = score;
                result = direction;
            }
        }
        return result;
    }

    /**
     * The node in the path-finding problem.
     */
    private final class PathNode implements HeuristicSearchSolver.Node<PathNode> {
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
            LinkedList<Direction> result = new LinkedList<>();
            for (PathNode node = this; node != null; node = node.previous)
                if (node.direction != null)
                    result.addFirst(node.direction);
            return result;
        }

        @Override
        public Iterable<PathNode> childNodes() {
            ArrayList<PathNode> result = new ArrayList<>(16);
            if (power < GameRules.powerConsumedPerMove)
                return result;
            for (Direction direction : Direction.values()) {
                PathNode next = new PathNode(this, direction);
                if (!next.position.inPlayArea()) continue;
                Drone drone = new StatefulDrone(position, new GameMap(map), coins, power, maxMoves - move);
                drone.move(direction);
                Position dronePosition = drone.getPosition();
                Station closeStation = map.closeStation(dronePosition);
                if (closeStation != null) {
                    next.map = drone.map;
                    if (plan.contains(closeStation.position)) {
                        next.plan = new ArrayList<>(plan);
                        next.plan.remove(closeStation.position);
                    }
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

        /**
         * Compares this node to the {@code other} based on the number of
         * remaining stations to visit, the amount of coins lost, and the
         * expected total distance to the next target.
         *
         * @see Comparable#compareTo
         */
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

        /**
         * {@inheritDoc}
         *
         * <p>Two nodes are considered equivalent when the latest
         * moves arrive at the same position.
         */
        @Override
        public boolean equivalent(PathNode o) {
            return o.position.equals(position);
        }
    }
}
