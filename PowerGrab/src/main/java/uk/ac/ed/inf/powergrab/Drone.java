package uk.ac.ed.inf.powergrab;

import java.util.Random;

/**
 * Base class for a drone which implements movement and resource transfer.
 */
public abstract class Drone {
    private Position position;
    private double coins, power;

    /**
     * This drone's random number generator
     */
    public final Random random;
    /**
     * Map of the play area
     */
    public final GameMap map;

    /**
     * Creates a drone. For use by child classes.
     *
     * @throws NullPointerException if {@code position} or {@code map} is {@code null}
     * @throws IllegalArgumentException if {@code coins} or {@code power} is negative
     */
    public Drone(Position position, GameMap map, double coins, double power) {
        if (position == null || map == null) throw new NullPointerException();
        if (coins < 0.0 || power < 0.0) throw new IllegalArgumentException();
        this.random = new Random();
        this.position = position;
        this.map = map;
        this.coins = coins;
        this.power = power;
    }

    public Position getPosition() { return position; }

    public double getCoins() { return coins; }

    public double getPower() { return power; }

    /**
     * Adds {@code coins} to the resources held by this drone. If {@code coins}
     * is negative this drone will lose at most what it holds.
     */
    public void addCoins(double coins) {
        this.coins += coins;
        if (this.coins < 0.0)
            this.coins = 0.0;
    }

    public void addPower(double power) {
        this.power += power;
        if (this.power < 0.0)
            this.power = 0.0;
    }

    /**
     * Move in the specified direction and update power. If the closest
     * station is withing a close distance (as defined in {@link GameRules}),
     * connect to it and transfer coins and power.
     */
    public void move(Direction direction) {
        if (power < GameRules.powerConsumedPerMove) {
            power = 0.0;
            return;
        }
        position = position.nextPosition(direction);
        power -= GameRules.powerConsumedPerMove;
        Station closeStation = map.closeStation(position);
        if (closeStation != null)
            closeStation.connect(this);
    }

    /**
     * Returns the direction this drone chose to move next.
     *
     * @return direction to move
     */
    public abstract Direction getDirection();
}
