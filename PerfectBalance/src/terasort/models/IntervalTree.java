package terasort.models;

import org.apache.hadoop.io.IntWritable;

/**
 * Implements fast class for interval tree queries.
 *
 */
public class IntervalTree {
    /**
     * Creates IntervalTree for given sorted list of splitPoints.
     * @param splitPoints Array of splitPoints in ascending order.
     */
    public IntervalTree(IntWritable[] splitPoints) {
        // TODO implementation
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * For given value returns number of interval it belongs to.
     * Intervals are sorted from 0 and their number is one bigger than number of splitPoints.
     *
     * @param value Value we're asking for
     * @return Number of interval containing value.
     */
    public int getInterval(IntWritable value) {
        // TODO implementation
        throw new java.lang.UnsupportedOperationException();
    }
}
