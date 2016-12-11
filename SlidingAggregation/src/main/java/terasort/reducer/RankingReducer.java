package terasort.reducer;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;
import terasort.models.IntervalTree;
import terasort.util.ConfigurationWrap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

public class RankingReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> implements Configurable {
    private ConfigurationWrap conf;
    private HashMap<Integer, Integer> windowSizes;

    private HashMap<Integer, Integer> getWindowSizes(Stream<Path> ps) throws IOException {
        HashMap<Integer, Integer> result = new HashMap<>();

        for(Path p : ps.toArray(Path[]::new)) {
            FSDataInputStream reader = conf.fileSystem.open(p);
            Scanner scanner = new Scanner(new InputStreamReader(reader));

            for(int i = 0; i < conf.numPartitions; ++i) {
                int partition, size;
                partition = scanner.nextInt();
                size = scanner.nextInt();
                result.put(partition, size);
            }

            scanner.close();
            reader.close();
        }

        return result;
    }

    public void setConf(Configuration _conf) {
        try {
            this.conf = new ConfigurationWrap(_conf);
            windowSizes = getWindowSizes(conf.getPaths("window.sizes.location"));
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return conf.conf;
    }

    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws InterruptedException, IOException {
        int size = 0;
        for (IntWritable ignored : values) {
            size++;
        }
        context.write(key, new IntWritable(size));
    }
}
