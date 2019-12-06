/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Program implements Runnable {
    private static final double INIT_COINS = 0.0, INIT_POWER = 250.0;
    private static final int MAX_MOVES = 250;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final LocalDate firstDate, lastDate;
    private final Position initialPosition;
    private final long seed;
    private final String droneType;
    private final Path localDirectory, logDirectory;
    private final boolean writeLog, writeStats;

    public static void main(String[] args) {
        if (args.length < 7) {
            System.err.println("Too few arguments!");
            System.out.println("Usage: powergrab <day> <month> <year> <latitude> <longitude> <seed> <drone type> \\");
            System.out.println("\t[-to <date>] [-dir <path>] [-o <path>] [-nolog] [-stats]");
            return;
        }
        Program program;
        try {
            try {
                program = new Program(Arrays.asList(args));
            } catch (NumberFormatException e) {
                System.err.println("Incorrectly formatted number given.");
                return;
            } catch (DateTimeParseException e) {
                System.err.println("Incorrectly formatted date given.");
                return;
            } catch (IllegalArgumentException e) {
                System.err.println("Erroneous argument given:");
                System.err.println(e.getMessage());
                return;
            }
            program.run();
        } catch (Throwable e) {
            System.err.println("Oops! Something went wrong while running the simulation.");
        }
    }

    public Program(List<String> args) {
        firstDate = LocalDate.of(Integer.parseInt(args.get(2)),
                Integer.parseInt(args.get(1)), Integer.parseInt(args.get(0)));
        initialPosition = new Position(Double.parseDouble(args.get(3)), Double.parseDouble(args.get(4)));
        if (!initialPosition.inPlayArea())
            throw new IllegalArgumentException("Initial position has to be inside the play area.");
        seed = Long.parseLong(args.get(5));
        droneType = args.get(6);
        int index;
        if ((index = args.indexOf("-to")) >= 0)
            lastDate = LocalDate.parse(args.get(index + 1), DATE_FORMAT);
        else
            lastDate = firstDate;
        if ((index = args.indexOf("-dir")) >= 0)
            localDirectory = Paths.get(args.get(index + 1));
        else
            localDirectory = null;
        if ((index = args.indexOf("-o")) >= 0)
            logDirectory = Paths.get(args.get(index + 1));
        else
            logDirectory = Paths.get(".");
        writeLog = !args.contains("-nolog");
        writeStats = args.contains("-stats");
    }

    @Override
    public void run() {
        Map<LocalDate, double[]> stats = new HashMap<>();
        for (LocalDate date = firstDate; date.compareTo(lastDate) <= 0; date = date.plusDays(1)) {
            System.out.printf("Drone: %s, Date: %s, Position: %s", droneType, date, initialPosition);
            System.out.println();
            Instant start = Instant.now();
            double score = run(date);
            Duration duration = Duration.between(start, Instant.now());
            double seconds = duration.getSeconds() + duration.getNano() * 1e-9;
            stats.put(date, new double[] { score, seconds });
            System.out.printf("Finished after %.3fs with score: %.1f%%", seconds, score * 100.0);
            System.out.println();
        }
        if (writeStats) {
            try (PrintWriter writer = new PrintWriter(
                    Paths.get(logDirectory.toString(), "performance-" + droneType + ".csv").toFile())) {
                for (Map.Entry<LocalDate, double[]> entry : stats.entrySet()) {
                    double[] values = entry.getValue();
                    writer.printf("%s,%f,%f", entry.getKey(), values[0], values[1]);
                    writer.println();
                }
            } catch (IOException e) {
                System.err.println("Could not save statistics.");
            }
        }
    }

    /**
     * Runs a drone simulation on the specified map of the day.
     *
     * @return the drone score
     */
    private double run(LocalDate date) {
        GeoJson geoJson;
        try {
            if (localDirectory == null) {
                String url = String.format(
                        "http://homepages.inf.ed.ac.uk/stg/powergrab/%04d/%02d/%02d/powergrabmap.geojson",
                        date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                geoJson = new GeoJson(new URL(url));
            } else {
                Path file = Paths.get(localDirectory.toString(),
                        String.format("%04d", date.getYear()),
                        String.format("%02d", date.getMonthValue()),
                        String.format("%02d", date.getDayOfMonth()),
                        "powergrabmap.geojson");
                geoJson = new GeoJson(file);
            }
        } catch (IOException e) {
            System.err.println("Failed loading map!");
            return -1.0;
        }
        return run(geoJson, writeLog ? date.format(DATE_FORMAT) : null);
    }

    /**
     * Runs a drone simulation on the specified map in GeoJSON format.
     *
     * @return the drone score
     */
    private double run(GeoJson geoJson, String fileSuffix) {
        GameMap map = geoJson.getMap();
        double totalCoins = 0.0;
        for (Station station : map.stations) {
            double coins = station.getCoins();
            if (coins > 0.0) totalCoins += coins;
        }
        Drone drone;
        switch (droneType) {
            case "stateless":
                drone = new StatelessDrone(initialPosition, map, INIT_COINS, INIT_POWER);
                break;
            case "stateful":
                drone = new StatefulDrone(initialPosition, map, INIT_COINS, INIT_POWER, MAX_MOVES);
                break;
            default:
                System.err.println("Unknown drone type!");
                return -1.0;
        }
        drone.random.setSeed(seed);
        Simulation simulation = new Simulation(drone, MAX_MOVES);
        List<Simulation.Move> moves = simulation.runSimulation();
        if (fileSuffix != null) {
            geoJson = new GeoJson(geoJson);
            geoJson.addMoves(moves);
            try (PrintWriter logWriter = new PrintWriter(
                    Paths.get(logDirectory.toString(),
                            droneType + "-" + fileSuffix + ".txt").toFile());
                    PrintWriter mapWriter = new PrintWriter(
                            Paths.get(logDirectory.toString(),
                                    droneType + "-" + fileSuffix + ".geojson").toFile())) {
                mapWriter.print(geoJson);
                boolean firstLine = true;
                for (Simulation.Move move : moves) {
                    if (firstLine)
                        firstLine = false;
                    else
                        logWriter.println();
                    logWriter.print(move);
                }
            } catch (IOException e) {
                System.err.println(e.toString());
                return -1.0;
            }
        }
        return drone.getCoins() / totalCoins;
    }
}
