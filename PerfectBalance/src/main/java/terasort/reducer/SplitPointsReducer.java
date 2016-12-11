package terasort.reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import terasort.SlidingAggregation;


public class SplitPointsReducer extends Reducer<IntWritable, IntWritable, NullWritable, IntWritable> implements Configurable {
    private int numPartition = 1;
    private Configuration conf;

    public void setConf(Configuration conf) {
        this.conf = conf;
        numPartition = this.conf.getInt("mapred.reduce.tasks", 5);
    }

    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        List<Integer> sortedList = new ArrayList<>();
        for (IntWritable i : values) {
            sortedList.add(i.get());
        }
        Collections.sort(sortedList);

        int counter = 1;
        int splitNumber = 1;
        int length = sortedList.size();
        int step = length / (numPartition<1 ? 1 : numPartition);
        System.out.println("Partition number: " + numPartition);
        for(Integer i : sortedList) {
            System.out.println("Number: " + i);
            if(counter % step == 0 && splitNumber < numPartition) {
                context.write(NullWritable.get(), new IntWritable(i));
                splitNumber++;
            }
            counter++;
        }
    }
}
