package terasort.reducer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SplitPointsReducer extends Reducer<IntWritable, IntWritable, NullWritable, IntWritable> {
    private static int numPartition;

    public static void setNumPartition(int numPartition) {
        SplitPointsReducer.numPartition = numPartition;
    }

    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        List<IntWritable> sortedList = new ArrayList<>();
        for (IntWritable i : values) {
            sortedList.add(i);
        }
        Collections.sort(sortedList);

        int counter = 1;
        int length = sortedList.size();
        int step = length / (numPartition + 1);
        for(IntWritable i : sortedList) {
            if(counter % step == 0) {
                context.write(NullWritable.get(), i);
            }
            counter++;
        }
    }
}
