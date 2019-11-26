package uk.ac.ed.inf.powergrab;

import uk.ac.ed.inf.powergrab.search.TspSolver;

import java.lang.Math;

/**
 * Stores a geographical position.
 */
public class Position implements TspSolver.Node<Position> {
    public final double latitude, longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the position after moving in the given direction.
     *
     * @see Direction
     * @return position after a move
     */
    public Position nextPosition(Direction direction) {
        // calculate the new position using the precomputed sin and cos
        return new Position(latitude + direction.cos * GameRules.moveDistance,
            longitude + direction.sin * GameRules.moveDistance);
    }

    /**
     * Returns {@code true} if this position is within the play area
     * defined in {@link GameRules}.
     *
     * @see GameRules
     * @return {@code true} if this position is within the play area
     */
    public boolean inPlayArea() {
        return GameRules.playArea.pointWithin(this);
    }

    /**
     * Returns the distance between this position and the parameter
     * expressed in degrees, calculated with Pythagoras' formula.
     *
     * @return distance between two positions
     */
    public double distance(Position position) {
        double dx = position.longitude - longitude;
        double dy = position.latitude - latitude;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns {@code true} if this position is close to the one given as
     * parameter, as defined in {@link GameRules}.
     *
     * @see GameRules
     * @return {@code true} if this position is close to the parameter
     */
    public boolean isClose(Position position) {
        return distance(position) < GameRules.closeDistance;
    }

    /**
     * Represents this position as a {@link String}
     * in the format "latitude,longitude".
     *
     * @return the {@code String} representation of this position
     */
    @Override
    public String toString() {
        return latitude + "," + longitude;
    }

    /**
     * @return {@code true} if the distance between this position and the parameter
     *         can be considered to be negligible
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Position) {
            Position other = (Position) obj;
            // equal if distance is less than about 1 mm
            return hashCode() == other.hashCode() && distance(other) < 1e-8;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // same hashcode if latitude and longitude equal with 1e-7 precision (about 1 cm)
        return (int)(Math.round(this.latitude * 1e7) ^ Math.round(this.longitude * 1e7));
    }
}
