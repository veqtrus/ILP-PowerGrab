package uk.ac.ed.inf.powergrab;

import org.junit.Test;

import java.util.stream.IntStream;

public class TestOutput {
    @Test
    public void makeTestOutput() {
        IntStream.rangeClosed(1, 12).mapToObj(Integer::toString).forEach(i -> {
            Program.main(new String[] {
                    i, i, "2019", "55.944425", "-3.188396", "5678", "stateless", "-o", "out" });
            Program.main(new String[] {
                    i, i, "2019", "55.944425", "-3.188396", "5678", "stateful", "-o", "out" });
        });
    }

    @Test
    public void testAll() {
        /*// Comment out for maven building
        IntStream.rangeClosed(2019, 2020).parallel().mapToObj(Integer::toString).forEach(i -> {
            Program.main(new String[] {
                    "01", "01", i, "55.944425", "-3.188396", "5678", "stateless", "-nolog", "-to", "31-12-" + i });
            Program.main(new String[] {
                    "01", "01", i, "55.944425", "-3.188396", "5678", "stateful", "-stats", "-nolog", "-to", "31-12-" + i });
        });*/
    }
}
