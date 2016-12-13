package terasort.partitioner;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Partitioner;

public class InputPartitioner<T extends Writable> extends Partitioner<IntWritable, T> {
    @Override
    public int getPartition(IntWritable key, T value, int numPartitions) {
        System.out.println("Partitions " + numPartitions + " value: " + key.get());
        return key.get() % numPartitions;
    }
}
