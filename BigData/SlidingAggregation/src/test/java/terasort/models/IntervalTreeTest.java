package terasort.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntervalTreeTest {
    @Test
    public void getInterval() throws Exception {
        final int partitions = 5;
        int[] result = new int[partitions-1];
        for(int i=0; i<partitions-1; i++) {
            result[i] = 10 * (i+1);
        }

        IntervalTree tree = new IntervalTree(result);

        for(int i=0; i < partitions * 10; i++) {
            assertEquals(i / 10, tree.getInterval(i));
        }
        tree.getInterval(1);
    }

}