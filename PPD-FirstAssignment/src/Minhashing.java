import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Minhashing {

    public static class ShingleMapper
            extends Mapper<Object, Text, IntWritable, IntWritable>{

        private final static int minShingleSize = 2;
        private final static int maxShingleSize = 10;
        static boolean onlyLetters = false;

        private StringBuilder[] currentShingles;

        private final static IntWritable one = new IntWritable(1);
        private IntWritable hash = new IntWritable();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            currentShingles = new StringBuilder[maxShingleSize + 1];
            String valueString = value.toString();

            for(int j=minShingleSize; j<=maxShingleSize; j++) {
                currentShingles[j] = new StringBuilder();
            }

            for(int i=0; i<valueString.length(); ++i) {
                char c = valueString.charAt(i);
                if(!onlyLetters || Character.isLetter(c)) {
                    for (int j = minShingleSize; j <= maxShingleSize; j++) {
                        if (currentShingles[j].length() == j) {
                            currentShingles[j].deleteCharAt(0);
                        }
                        currentShingles[j].append(c);
                        if (currentShingles[j].length() == j) {
                            hash.set(currentShingles[j].hashCode());
                            context.write(hash, one);
                        }
                    }
                }
            }

        }
    }

    public static class ShingleSumReducer
            extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    // public static class MinhashingMapper extends Mapper<>

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job1 = Job.getInstance(conf, "shingles");
        job1.setJarByClass(Minhashing.class);

        ShingleMapper.onlyLetters = args.length >= 4 && args[3].equals("true");
        job1.setMapperClass(ShingleMapper.class);

        job1.setCombinerClass(ShingleSumReducer.class);
        job1.setReducerClass(ShingleSumReducer.class);
        job1.setOutputKeyClass(IntWritable.class);
        job1.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1]));

        if(!job1.waitForCompletion(true)) {
            System.exit(1);
        }

        /*Job job2 = Job.getInstance(conf, "minhashing");
        job2.setJarByClass(Minhashing.class);

        job2.setMapperClass();
        job2.setCombinerClass();
        job2.setReducerClass();
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job1, new Path(args[1]));

        FileOutputFormat.setOutputPath(job2, new Path(args[2]));
        System.exit(job2.waitForCompletion(true) ? 0 : 2);*/
    }
}