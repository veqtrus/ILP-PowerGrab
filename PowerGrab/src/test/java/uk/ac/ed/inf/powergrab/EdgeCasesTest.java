/*=================================================================
Copyright 2019 Pavlos Georgiou

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at <https://mozilla.org/MPL/2.0/>.
=================================================================*/

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
        final String fileName = "surrounded.geojson";
        if (!Files.exists(Paths.get(fileName))) return;
        Position initPos = new Position(55.944425, -3.188396);
        GeoJson geoJson = getMap(fileName);
        Program.run(geoJson, initPos, 5678, "stateful", "surrounded");
    }
}
