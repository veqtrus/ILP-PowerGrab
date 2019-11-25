package uk.ac.ed.inf.powergrab;

import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;

public class PerformanceTest {
    public void testPerformance(String droneType) {
        LocalDate date = LocalDate.of(2019, 1, 1);
        LocalDate lastDate = LocalDate.of(2020, 12, 30);
        Position initPos = new Position(55.944425, -3.188396);
        long seed = 5678;
        HashMap<LocalDate, Double[]> scores = new HashMap<LocalDate, Double[]>();
        for (; date.compareTo(lastDate) <= 0; date = date.plusDays(1)) {
            Instant start = Instant.now();
            double score = Program.run(date, initPos, seed, droneType, true);
            Duration duration = Duration.between(start, Instant.now());
            double seconds = duration.getSeconds() + duration.getNano() * 1e-9;
            scores.put(date, new Double[] { score, seconds });
            System.out.printf("Performance on %s: %.1f%% %.3fs\n", date, score * 100.0, seconds);
        }
        try {
            FileWriter writer = new FileWriter("performance-" + droneType + ".csv");
            for (LocalDate key : scores.keySet()) {
                Double[] score = scores.get(key);
                writer.write(key.toString() + "," + score[0] + "," + score[1] + "\r\n");
            }
            writer.close();
        } catch (IOException e) {}
    }

    @Test
    public void testStatelessPerformance() {
        testPerformance("stateless");
    }

    @Test
    public void testStatefulPerformance() {
        testPerformance("stateful");
    }

    @Test
    public void testAttractionPerformance() {
        testPerformance("attraction");
    }
}
