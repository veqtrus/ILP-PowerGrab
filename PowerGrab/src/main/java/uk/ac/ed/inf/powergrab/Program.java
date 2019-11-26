package uk.ac.ed.inf.powergrab;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

public class Program {
    public static final double INIT_COINS = 0.0, INIT_POWER = 250.0;
    public static final int MAX_MOVES = 250;

    public static void main(String[] args) {
        if (args.length < 7) {
            System.err.println("Too few arguments!");
            return;
        }
        LocalDate date = LocalDate.parse(args[2] + "-" + args[1] + "-" + args[0]);
        Position initPos = new Position(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        System.out.println("Date: " + date.toString() + ", Position: " + initPos.toString());
        double score = run(date, initPos, Long.parseLong(args[5]), args[6], true);
        System.out.printf("Finished with score: %.1f%%\n", score * 100.0);
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
        String formattedDate = String.format("%02d-%02d-%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
        GeoJson geojson;
        try {
            geojson = new GeoJson(new URL(url));
        } catch (IOException e) {
            System.err.println("Failed downloading map!");
            return -1.0;
        }
        return run(geojson, initPos, seed, droneType, writeLog ? formattedDate : null);
    }

    /**
     * Runs a drone simulation on the specified map in GeoJSON format.
     *
     * @return the drone score
     */
    public static double run(GeoJson geojson, Position initPos, long seed, String droneType, String fileName) {
        GameMap map = geojson.getMap();
        double totalCoins = 0.0;
        for (Station station : map.stations) {
            double coins = station.getCoins();
            if (coins > 0.0) totalCoins += coins;
        }
        Drone drone;
        if (droneType.equals("stateless")) {
            drone = new StatelessDrone(initPos, map, INIT_COINS, INIT_POWER);
        } else if (droneType.equals("stateful")) {
            drone = new StatefulDrone(initPos, map, INIT_COINS, INIT_POWER, MAX_MOVES);
        } else if (droneType.equals("attraction")) {
            drone = new AttractionDrone(initPos, map, INIT_COINS, INIT_POWER);
        } else {
            System.err.println("Unknown drone type!");
            return -1.0;
        }
        drone.random.setSeed(seed);
        Simulation simulation = new Simulation(drone, MAX_MOVES);
        List<Simulation.Move> moves = simulation.runSimulation();
        if (fileName != null) {
            geojson = new GeoJson(geojson);
            geojson.addMoves(moves);
            FileWriter logWriter, mapWriter;
            try {
                logWriter = new FileWriter(droneType + "-" + fileName + ".txt");
                mapWriter = new FileWriter(droneType + "-" + fileName + ".geojson");
                for (Simulation.Move move : moves)
                    logWriter.write(move.toString() + "\r\n");
                mapWriter.write(geojson.toString());
            } catch (IOException e) {
                System.err.println(e.toString());
                return -1.0;
            }
            try {
                logWriter.close();
                mapWriter.close();
            } catch (IOException e) {
                System.err.println(e.toString());
                return -1.0;
            }
        }
        return drone.getCoins() / totalCoins;
    }
}
