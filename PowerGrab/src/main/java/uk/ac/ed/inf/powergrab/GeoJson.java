/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

import org.json.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Stores the contents of a GeoJSON file.
 */
public class GeoJson {
    private final JSONObject featureCollection;

    /**
     * Creates a {@link GeoJson} object from from another.
     */
    public GeoJson(GeoJson geoJson) {
        this(geoJson.featureCollection);
    }

    /**
     * Creates a {@link GeoJson} object from a map encoded in a {@linkplain JSONObject JSON object}.
     */
    public GeoJson(JSONObject json) {
        this(json.toString());
    }

    /**
     * Creates a {@link GeoJson} object from a map encoded in a {@code json} {@link String}.
     */
    public GeoJson(String json) {
        featureCollection = new JSONObject(json);
    }

    /**
     * Creates a {@link GeoJson} object from the map downloaded from {@code url}.
     *
     * @throws IOException if an I/O exception occurs while downloading the map
     */
    public GeoJson(URL url) throws IOException {
        StringBuilder builder = new StringBuilder();
        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext())
            builder.append(scanner.next());
        scanner.close();
        featureCollection = new JSONObject(builder.toString());
    }

    /**
     * Creates a {@link GeoJson} object from the map loaded from {@code file}.
     *
     * @throws IOException if an I/O exception occurs while loading the map
     */
    public GeoJson(Path file) throws IOException {
        this(new String(Files.readAllBytes(file)));
    }

    /**
     * Returns a {@link GameMap} containing the stations in the GeoJSON file.
     *
     * @see GameMap
     * @throws JSONException if decoding fails
     * @return map of stations
     */
    public GameMap getMap() {
        GameMap map = new GameMap();
        JSONArray features = featureCollection.getJSONArray("features");
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            JSONArray coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates");
            String id = properties.getString("id");
            double coins = properties.getDouble("coins");
            double power = properties.getDouble("power");
            double longitude = coordinates.getDouble(0);
            double latitude = coordinates.getDouble(1);
            Position position = new Position(latitude, longitude);
            map.stations.add(new Station(id, position, coins, power));
        }
        return map;
    }

    /**
     * Adds a trace of a drone's moves.
     */
    public void addMoves(List<Simulation.Move> moves) {
        JSONArray features = featureCollection.getJSONArray("features");
        JSONArray coordinates = new JSONArray();
        boolean first = true;
        for (Simulation.Move move : moves) {
            JSONArray after = new JSONArray();
            after.put(move.after.longitude);
            after.put(move.after.latitude);
            if (first) {
                JSONArray before = new JSONArray();
                before.put(move.before.longitude);
                before.put(move.before.latitude);
                coordinates.put(before);
                first = false;
            }
            coordinates.put(after);
        }
        JSONObject geometry = new JSONObject(), line = new JSONObject();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);
        line.put("type", "Feature");
        line.put("geometry", geometry);
        line.put("properties", new JSONObject());
        features.put(line);
    }

    /**
     * Encodes this GeoJSON as a {@code String}.
     *
     * @return JSON
     */
    @Override
    public String toString() {
        return featureCollection.toString();
    }
}
