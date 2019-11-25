package uk.ac.ed.inf.powergrab;

import java.lang.Math;

public enum Direction {
    N(0), NNE(1), NE(2), ENE(3),
    E(4), ESE(5), SE(6), SSE(7),
    S(8), SSW(9), SW(10), WSW(11),
    W(12), WNW(13), NW(14), NNW(15);

    private final int direction;

    /**
     * Precomputed sin value for this direction's {@linkplain #getAngle angle}
     */
    public final double sin;
    /**
     * Precomputed cos value for this direction's {@linkplain #getAngle angle}
     */
    public final double cos;

    Direction(int direction) {
        this.direction = direction;
        double angle = getAngle();
        this.sin = Math.sin(angle);
        this.cos = Math.cos(angle);
    }

    /**
     * Returns the clockwise angle from the north for this direction.
     *
     * @return angle in radians
     */
    public double getAngle() {
        return direction * Math.PI / 8.0;
    }
}
