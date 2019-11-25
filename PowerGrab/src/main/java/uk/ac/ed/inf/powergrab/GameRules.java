package uk.ac.ed.inf.powergrab;

/**
 * Class containing static fields about the games's rules
 */
public class GameRules {
    /**
     * A drone within this distance to a station can connect with it.
     */
    public static double closeDistance = 0.00025;

    /**
     * Amount of power consumed by a drone in each move
     */
    public static double powerConsumedPerMove = 1.25;

    /**
     * How far a drone moves in step
     */
    public static double moveDistance = 0.0003;

    /**
     * The area a drone can move within
     */
    public static Rectangle playArea = new Rectangle(55.946233, -3.192473, 55.942617, -3.184319);
}
