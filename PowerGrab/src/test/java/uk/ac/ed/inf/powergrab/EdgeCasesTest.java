package uk.ac.ed.inf.powergrab;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EdgeCasesTest {
    private GeoJson getMap(String fileName) {
        try {
            return new GeoJson(new String(Files.readAllBytes(Paths.get(fileName))));
        } catch (IOException e) {
            return null;
        }
    }

    @Test
    public void testSurroundedByNegatives() {
        Position initPos = new Position(55.944425, -3.188396);
        long seed = 5678;
        Program.run(getMap("surrounded.geojson"), initPos, seed, "stateful", "surrounded");
    }
}
