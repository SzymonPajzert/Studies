package terasort.mapper;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import terasort.util.ConfigurationWrap;
import terasort.util.PairInt;

import java.io.IOException;
import java.util.HashMap;

import java.util.Scanner;

public class RemoteRelevanceMapper extends Mapper<Object, Text, IntWritable, PairInt> implements Configurable {
    private int numberOfElements;
    private static boolean sendRemotelyRelevant;
    private static int windowSize;
    private ConfigurationWrap conf;

    public static void setWindowSize(int windowSize) {
        RemoteRelevanceMapper.windowSize = windowSize;
    }

    public static void setSendRemotelyRelevant(boolean sendRemotelyRelevant) {
        RemoteRelevanceMapper.sendRemotelyRelevant = sendRemotelyRelevant;
    }

    public void setConf(Configuration conf) {
        try {
            this.conf = new ConfigurationWrap(conf);
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return conf.conf;
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(value.toString());

        int ranking = scanner.nextInt();
        int val = scanner.nextInt();
        PairInt record = new PairInt(ranking, val);

        scanner.close();

        IntWritable firstKey = new IntWritable(conf.getReducer(ranking));

        System.out.println("Mapping " + firstKey.get() + " " + record);
        context.write(firstKey, record);

        if(sendRemotelyRelevant && ranking - windowSize > 0) {
            int secondKeyValue = conf.getReducer(ranking - windowSize);
            if(secondKeyValue != firstKey.get()) {
                IntWritable secondKey = new IntWritable(secondKeyValue);
                System.out.println("Mapping " + secondKey.get() + " " + record);
                context.write(secondKey, record);
            }
        }
    }
}
