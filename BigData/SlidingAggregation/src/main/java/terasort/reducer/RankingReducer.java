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
import java.util.*;
import java.util.stream.Stream;

public class RankingReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> implements Configurable {
    private ConfigurationWrap conf;
    private HashMap<Integer, Integer> windowSizes;

    public void setConf(Configuration _conf) {
        try {
            this.conf = new ConfigurationWrap(_conf);
            windowSizes = conf.getWindowSizes();
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return conf.conf;
    }

    private int getPrefixRanking(int key) {
        int result = 0;
        for(Map.Entry<Integer, Integer> entry : windowSizes.entrySet()) {
            if(entry.getKey() < key) {
                result += entry.getValue();
            }
        }
        return result;
    }

    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws InterruptedException, IOException {
        List<Integer> sortedList = new ArrayList<>();
        for (IntWritable i : values) {
            sortedList.add(i.get());
        }
        Collections.sort(sortedList);

        int prefix = getPrefixRanking(key.get());
        for(Integer i : sortedList) {
            prefix++;
            context.write(new IntWritable(prefix), new IntWritable(i));
        }
    }
}
