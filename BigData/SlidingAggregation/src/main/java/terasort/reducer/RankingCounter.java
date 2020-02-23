package terasort.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class RankingCounter extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws InterruptedException, IOException {
        int size = 0;
        for (IntWritable ignored : values) {
            size++;
        }
        context.write(key, new IntWritable(size));
    }
}
