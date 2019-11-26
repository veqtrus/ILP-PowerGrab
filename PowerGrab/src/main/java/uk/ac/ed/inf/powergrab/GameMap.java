package uk.ac.ed.inf.powergrab;


import java.util.ArrayList;
import java.util.List;

/**
 * A map of the play area, contains {@link Station}s.
 */
public class GameMap {
    public final List<Station> stations;

    /**
     * Creates an empty map.
     */
    public GameMap() {
        this.stations = new ArrayList<Station>();
    }

    /**
     * Creates a map and copies the stations from the given map.
     */
    public GameMap(GameMap map) {
        this.stations = new ArrayList<Station>(map.stations.size());
        for (Station station : map.stations)
            this.stations.add(new Station(station));
    }

    /**
     * Returns the {@link Station} in this map whose position is closest
     * to the given {@code position}, or {@code null} if the map is empty.
     *
     * @return station closest to {@code position} or {@code null}
     */
    public Station nearestStation(Position position) {
        Station result = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (Station station : stations) {
            double distance = station.position.distance(position);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                result = station;
            }
        }
        return result;
    }
}
