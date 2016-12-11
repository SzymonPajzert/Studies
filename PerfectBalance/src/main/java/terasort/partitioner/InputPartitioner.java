package terasort.partitioner;

import terasort.models.IntervalTree;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

import java.io.IOException;

public class InputPartitioner extends Partitioner<IntWritable, IntWritable> implements Configurable {
    private int numPartitions;
    private static Path splitPointsPath;
    private Configuration conf = null;
    private IntervalTree tree;

    public static void setSplitPointsPath(Path splitPointsPath) {
        InputPartitioner.splitPointsPath = splitPointsPath;
    }

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
        try {
            LocalFileSystem ie = FileSystem.getLocal(conf);
            this.conf = conf;
            this.numPartitions = this.conf.getInt("mapreduce.job.reduces", 5);
            int[] splitPoints = readSplitPoints(ie, splitPointsPath, conf);
            this.tree = new IntervalTree(splitPoints);
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return this.conf;
    }
}
