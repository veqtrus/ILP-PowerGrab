/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

package uk.ac.ed.inf.powergrab;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PerformanceTest {
    public void testPerformance(String droneType) {
        LocalDate date = LocalDate.of(2019, 1, 1);
        LocalDate lastDate = LocalDate.of(2020, 12, 30);
        Position initPos = new Position(55.944425, -3.188396);
        long seed = 5678;
        Map<LocalDate, double[]> scores = new HashMap<>();
        for (; date.compareTo(lastDate) <= 0; date = date.plusDays(1)) {
            Instant start = Instant.now();
            double score = Program.run(date, initPos, seed, droneType, true);
            Duration duration = Duration.between(start, Instant.now());
            double seconds = duration.getSeconds() + duration.getNano() * 1e-9;
            scores.put(date, new double[] { score, seconds });
            System.out.printf("Performance on %s: %.1f%% %.3fs", date, score * 100.0, seconds);
            System.out.println();
        }
        try (PrintWriter writer = new PrintWriter("performance-" + droneType + ".csv")) {
            for (Map.Entry<LocalDate, double[]> entry : scores.entrySet()) {
                double[] score = entry.getValue();
                writer.printf("%s,%f,%f", entry.getKey(), score[0], score[1]);
                writer.println();
            }
        } catch (IOException e) {
            System.err.println("Could not save performance log.");
        }
    }

    @Test
    public void testStatelessPerformance() {
        testPerformance("stateless");
    }

    @Test
    public void testStatefulPerformance() {
        testPerformance("stateful");
    }
}
