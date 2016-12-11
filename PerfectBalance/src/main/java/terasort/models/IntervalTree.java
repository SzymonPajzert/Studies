package terasort.models;

import org.apache.hadoop.io.IntWritable;

/**
 * Implements fast class for interval tree queries.
 *
 */
public class IntervalTree {
    private final int[] splitPoints;

    /**
     * Creates IntervalTree for given sorted list of splitPoints.
     * @param splitPoints Array of splitPoints in ascending order.
     */
    public IntervalTree(int[] splitPoints) {
        this.splitPoints = new int[splitPoints.length];

        int counter = 0;
        for(int i : splitPoints) {
            this.splitPoints[counter] = i;
            counter++;
        }
    }

    /**
     * For given value returns number of interval it belongs to.
     * Intervals are sorted from 0 and their number is one bigger than number of splitPoints.
     *
     * @param value Value we're asking for
     * @return Number of interval containing value.
     */
    public int getInterval(int value) {
        int counter = 0;
        for(int i : splitPoints) {
            if(value < i) {
                return counter;
            }
            counter++;
        }
        return counter;
    }
}
