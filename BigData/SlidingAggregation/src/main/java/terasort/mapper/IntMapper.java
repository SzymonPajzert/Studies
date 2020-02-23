package terasort.mapper;


import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import terasort.models.IntervalTree;
import terasort.util.ConfigurationWrap;

import java.io.IOException;
import java.util.StringTokenizer;

public class IntMapper extends Mapper<Object, Text, IntWritable, IntWritable> implements Configurable {
    private IntervalTree tree;
    private ConfigurationWrap conf;
    private IntWritable key = new IntWritable();

    public void setConf(Configuration _conf) {
        try {
            this.conf = new ConfigurationWrap(_conf);
            this.tree = new IntervalTree(conf.readSplitPoints());
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return conf.conf;
    }

    private int getReducer(int number) {
        int interval = tree.getInterval(number);
        System.out.println("In interval tree:" + tree);
        System.out.println("For number " + number + " interval " + interval);
        return interval;
    }


    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line);

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int number = Integer.parseInt(token);
            this.key.set(getReducer(number));
            context.write(this.key, new IntWritable(number));
        }
    }


}
