package terasort.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

public class SamplingIntMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
    private static int samplingRate = 100;  // Sampling rate for mapper - we want 1/samplingRate elements to be sent.
    private static Random generator = new Random();
    private static IntWritable one = new IntWritable(1);

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line);

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if(generator.nextInt(samplingRate) == 0) {
                int number = Integer.parseInt(token);
                context.write(one, new IntWritable(number));
            }
        }


    }
}
