/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

/**
 * A {@code Rectangle} specifies an area in the geographic coordinate space
 * that is enclosed by the {@code Rectangle} object's {@code topLeft} and
 * {@code bottomRight} {@linkplain Position positions}.
 *
 * @see Position
 */
public class Rectangle {
    public final Position topLeft, bottomRight;

    /**
     * Creates a rectangle from the {@code topLeft} and {@code bottomRight}
     * {@linkplain Position positions}.
     */
    public Rectangle(Position topLeft, Position bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    /**
     * Creates a rectangle from the {@code top left} and {@code bottom right}
     * coordinates.
     */
    public Rectangle(double top, double left, double bottom, double right) {
        this.topLeft = new Position(top, left);
        this.bottomRight = new Position(bottom, right);
    }

    /**
     * Returns {@code true} if {@code position} is inside this rectangle.
     *
     * @return {@code true} if position inside rectangle
     */
    public boolean pointWithin(Position position) {
        return position.latitude < topLeft.latitude && position.latitude > bottomRight.latitude
            && position.longitude > topLeft.longitude && position.longitude < bottomRight.longitude;
    }
}
