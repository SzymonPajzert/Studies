package terasort.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Random;

public class SamplingIntMapper extends Mapper<Object, IntWritable, IntWritable, IntWritable> {
    private static int samplingRate = 10000;  // Sampling rate for mapper - we want 1/samplingRate elements to be sent.
    private static Random generator = new Random();
    private static IntWritable one = new IntWritable(1);

    @Override
    public void map(Object key, IntWritable value, Context context) throws IOException, InterruptedException {
        if(generator.nextInt(samplingRate) == 0) {
            context.write(one, value);
        }
    }
}
