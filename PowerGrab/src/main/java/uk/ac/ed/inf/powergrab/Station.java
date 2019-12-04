/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

/**
 * Represents a station in the game.
 */
public class Station {
    public final String id;
    public final Position position;
    private double coins, power;

    /**
     * Creates a station with the given parameters.
     *
     * @throws NullPointerException if {@code id} or {@code position} is {@code null}
     */
    public Station(String id, Position position, double coins, double power) {
        if (id == null || position == null) throw new NullPointerException();
        this.id = id;
        this.position = position;
        this.coins = coins;
        this.power = power;
    }

    /**
     * Creates station using the parameters of the given station.
     */
    public Station(Station station) {
        this.id = station.id;
        this.position = station.position;
        this.coins = station.coins;
        this.power = station.power;
    }

    public double getCoins() { return coins; }

    public double getPower() { return power; }

    /**
     * If the drone is close to this station,
     * connect and transfer coins and power.
     */
    public void connect(Drone drone) {
        if (drone.getPosition().distance(position) >= GameRules.closeDistance) return;
        double droneCoins = drone.getCoins(), dronePower = drone.getPower();
        drone.addCoins(coins);
        drone.addPower(power);
        coins += droneCoins;
        power += dronePower;
        // if coins or power end up positive it means that all of the resource
        // has been transferred and the station will be empty
        if (coins > 0.0)
            coins = 0.0;
        if (power > 0.0)
            power = 0.0;
    }

    @Override
    public String toString() {
        return "Station id: " + id + ", position: (" + position.toString() + "), coins: " + coins + ", power: " + power;
    }

    /**
     * Returns {@code true} if the stations are the same object
     * or their {@code id}s are equal.
     *
     * @return {@code true} if the stations are considered equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Station) {
            Station station = (Station) obj;
            return station.id.equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }
}
