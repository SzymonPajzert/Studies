package terasort.partitioner;

import org.apache.hadoop.fs.*;
import terasort.SlidingAggregation;
import terasort.models.IntervalTree;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InputPartitioner extends Partitioner<IntWritable, IntWritable> implements Configurable {
    private int numPartitions;
    private Configuration conf = null;
    private IntervalTree tree;

    @Override
    public int getPartition(IntWritable key, IntWritable value, int numPartitions) {
        return tree.getInterval(key.get());
    }

    private int[] readSplitPoints(FileSystem fs, Path p, Configuration conf) throws IOException {
        int[] result = new int[numPartitions- 1];
        FSDataInputStream reader = fs.open(p);

        for(int i = 0; i < numPartitions - 1; ++i) {
            IntWritable value = new IntWritable();
            value.readFields(reader);
            result[i] = value.get();
        }

        reader.close();
        return result;
    }

    public void setConf(Configuration conf) {
        FileSystem ie = null;
        try {
            ie = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.conf = conf;
        this.numPartitions = this.conf.getInt("mapreduce.job.reduces", 5);

        String root = this.conf.get("fs.default.name");
        String path = root + "/user/szymonpajzert/" + this.conf.get("partition.location");
        System.out.println("Partition location: " + path);

        List<FileStatus> statuses = null;
        try {
            statuses = Arrays.asList(ie.listStatus(new Path(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileStatus status = statuses.stream()
                .filter(x -> x.getLen() > 0)
                .findFirst()
                .get();


        int[] splitPoints = null;
        try {
            splitPoints = readSplitPoints(ie, status.getPath(), conf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.tree = new IntervalTree(splitPoints);
        /*} catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        } */
    }

    public Configuration getConf() {
        return this.conf;
    }
}
