package terasort.partitioner;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class InputPartitioner extends Partitioner<IntWritable, IntWritable> {
    @Override
    public int getPartition(IntWritable key, IntWritable value, int numPartitions) {
        System.out.println("Partitions " + numPartitions + " value: " + key.get());
        return key.get() % numPartitions;
    }
}
