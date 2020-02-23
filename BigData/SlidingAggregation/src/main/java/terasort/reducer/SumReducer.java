package terasort.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;
import terasort.util.PairInt;

import java.io.IOException;

public class SumReducer extends Reducer<IntWritable, PairInt, IntWritable, IntWritable> {
    @Override
    public void reduce(IntWritable key, Iterable<PairInt> values, Context context) throws IOException, InterruptedException {
        int sum = 0;
        for(PairInt value : values) {
            sum += value.getY();
        }
        context.write(key, new IntWritable(sum));
    }
}
