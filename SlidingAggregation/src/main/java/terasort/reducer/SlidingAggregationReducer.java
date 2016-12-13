package terasort.reducer;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;
import terasort.util.ConfigurationWrap;
import terasort.util.PairInt;

import java.io.IOException;
import java.util.*;

public class SlidingAggregationReducer extends Reducer<IntWritable, PairInt, IntWritable, IntWritable> implements Configurable {
    private int windowSize;
    private ConfigurationWrap conf;
    // private HashMap<Integer, Integer> windowsAggregates;


    public void setConf(Configuration conf) {
        try {
            this.conf = new ConfigurationWrap(conf);
            this.windowSize = conf.getInt("window.size", 1);
            // this.windowsAggregates = this.conf.getWindowAggregate();
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return conf.conf;
    }

    @Override
    public void reduce(IntWritable key, Iterable<PairInt> values, Context context) throws IOException, InterruptedException {
        SortedMap<Integer, Integer> sorted = new TreeMap<>();
        for (PairInt i : values) {
            sorted.put(i.getX(), i.getY());
        }

        Queue<Integer> queue = new LinkedList<>();
        int sum = 0;

        for (SortedMap.Entry<Integer, Integer> pair : sorted.entrySet()) {
            int x = pair.getKey();
            int y = pair.getValue();

            System.out.println("Read " + x + " " + y);
            System.out.println("Queue size" + queue.size());
            System.out.println("Window size " + windowSize);

            queue.add(y);
            sum+=y;
            if(queue.size() == windowSize) {
                context.write(new IntWritable(x), new IntWritable(sum));
                sum -= queue.poll();
            }
        }

    }
}
