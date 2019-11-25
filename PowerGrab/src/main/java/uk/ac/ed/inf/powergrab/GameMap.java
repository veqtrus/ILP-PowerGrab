package uk.ac.ed.inf.powergrab;

import java.util.*;

/**
 * A map of the play area, contains {@link Station}s.
 */
public class GameMap {
    /**
     * Class implementing the {@link Comparator} interface for {@link Station}s.
     * Comparison is done based on the distance of stations to the given position.
     */
    private static class DistanceComparator implements Comparator<Station> {
        private final Position position;

        public DistanceComparator(Position position) {
            this.position = position;
        }

        public int compare(Station s1, Station s2) {
            double d1 = s1.position.distance(position), d2 = s2.position.distance(position);
            return Double.compare(d1, d2);
        }
    }

    private final Map<String, Station> stations;

    /**
     * Creates an empty map.
     */
    public GameMap() {
        this.stations = new HashMap<String, Station>();
    }

    /**
     * Creates a map and copies the stations from the given map.
     */
    public GameMap(GameMap map) {
        this.stations = new HashMap<String, Station>(map.stations.size());
        for (Station station : map.stations.values())
            this.stations.put(station.id, new Station(station));
    }

    /**
     * Returns a collection of all the stations in this map.
     *
     * @return all the stations in this map
     */
    public Collection<Station> stations() {
        return stations.values();
    }

    /**
     * Returns the {@link Station} in this map that has the given {@code id},
     * or {@code null} if no such station can be found.
     *
     * @param id station id
     * @return station or {@code null} if not found
     */
    public Station getStationById(String id) {
        return stations.get(id);
    }

    /**
     * Adds a station to the map.
     *
     * @throws NullPointerException if {@code station} is {@code null}
     */
    public void addStation(Station station) {
        if (station == null) throw new NullPointerException();
        stations.put(station.id, station);
    }

    /**
     * Returns the {@link Station} in this map whose position is closest
     * to the given {@code position}, or {@code null} if the map is empty.
     *
     * @return station closest to {@code position} or {@code null}
     */
    public Station nearestStation(Position position) {
        if (stations.size() == 0) return null;
        return Collections.min(stations.values(), new DistanceComparator(position));
    }
}
