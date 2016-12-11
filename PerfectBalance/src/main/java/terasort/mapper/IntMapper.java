package terasort.mapper;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import terasort.models.IntervalTree;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class IntMapper extends Mapper<Object, Text, IntWritable, IntWritable> implements Configurable {
    private int numPartitions;
    private IntervalTree tree;
    private Configuration conf;
    private IntWritable key = new IntWritable();

    private int[] readSplitPoints(FileSystem fs, Path p) throws IOException {
        int[] result = new int[numPartitions- 1];
        FSDataInputStream reader = fs.open(p);

        Scanner scanner = new Scanner(new InputStreamReader(reader));
        for(int i = 0; i < numPartitions - 1; ++i) {
            result[i] = scanner.nextInt();
            System.out.println("Read " + result[i]);
        }

        scanner.close();
        reader.close();
        return result;
    }

    private Path getSplitPath(FileSystem fs, Configuration conf) throws IOException {
        String root = conf.get("fs.default.name");
        String path = root + "/user/szymonpajzert/" + conf.get("partition.location");
        System.out.println("Partition location: " + path);

        List<FileStatus> statuses = Arrays.asList(fs.listStatus(new Path(path)));
        FileStatus status = statuses.stream()
                .filter(x -> x.getLen() > 0)
                .findFirst()
                .get();

        return status.getPath();
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        try {
            FileSystem ie = FileSystem.get(conf);
            this.numPartitions = conf.getInt("mapreduce.job.reduces", 5);
            Path splitPath = getSplitPath(ie, conf);
            this.tree = new IntervalTree(readSplitPoints(ie, splitPath));
        } catch (IOException e) {
            throw new IllegalArgumentException("can't read partitions file", e);
        }
    }

    public Configuration getConf() {
        return this.conf;
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
