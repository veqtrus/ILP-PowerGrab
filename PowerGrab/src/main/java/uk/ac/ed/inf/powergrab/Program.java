package uk.ac.ed.inf.powergrab;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Program {
    private static final double INIT_COINS = 0.0, INIT_POWER = 250.0;
    private static final int MAX_MOVES = 250;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        if (args.length < 7) {
            System.err.println("Too few arguments!");
            return;
        }
        LocalDate date = LocalDate.parse(args[2] + "-" + args[1] + "-" + args[0]);
        Position initPos = new Position(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        System.out.println("Date: " + date.toString() + ", Position: " + initPos.toString());
        double score = run(date, initPos, Long.parseLong(args[5]), args[6], true);
        System.out.printf("Finished with score: %.1f%%", score * 100.0);
        System.out.println();
    }

    /**
     * Runs a drone simulation on the specified map of the day.
     *
     * @return the drone score
     */
    public static double run(LocalDate date, Position initPos, long seed, String droneType, boolean writeLog) {
        String url = String.format(
            "http://homepages.inf.ed.ac.uk/stg/powergrab/%04d/%02d/%02d/powergrabmap.geojson",
            date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        GeoJson geojson;
        try {
            geojson = new GeoJson(new URL(url));
        } catch (IOException e) {
            System.err.println("Failed downloading map!");
            return -1.0;
        }
        return run(geojson, initPos, seed, droneType, writeLog ? date.format(DATE_FORMAT) : null);
    }

    /**
     * Runs a drone simulation on the specified map in GeoJSON format.
     *
     * @return the drone score
     */
    public static double run(GeoJson geoJson, Position initPos, long seed, String droneType, String fileSuffix) {
        GameMap map = geoJson.getMap();
        double totalCoins = 0.0;
        for (Station station : map.stations) {
            double coins = station.getCoins();
            if (coins > 0.0) totalCoins += coins;
        }
        Drone drone;
        switch (droneType) {
            case "stateless":
                drone = new StatelessDrone(initPos, map, INIT_COINS, INIT_POWER);
                break;
            case "stateful":
                drone = new StatefulDrone(initPos, map, INIT_COINS, INIT_POWER, MAX_MOVES);
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
            try (PrintWriter logWriter = new PrintWriter(droneType + "-" + fileSuffix + ".txt");
                    PrintWriter mapWriter = new PrintWriter(droneType + "-" + fileSuffix + ".geojson")) {
                mapWriter.print(geoJson);
                for (Simulation.Move move : moves)
                    logWriter.println(move);
            } catch (IOException e) {
                System.err.println(e.toString());
                return -1.0;
            }
        }
        return drone.getCoins() / totalCoins;
    }
}
