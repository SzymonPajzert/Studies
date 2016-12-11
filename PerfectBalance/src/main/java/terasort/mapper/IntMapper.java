package terasort.mapper;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class IntMapper extends Mapper<Object, IntWritable, IntWritable, IntWritable> {
    @Override
    public void map(Object key, IntWritable value, Context context) throws IOException, InterruptedException {
        context.write(value, value);
    }
}
