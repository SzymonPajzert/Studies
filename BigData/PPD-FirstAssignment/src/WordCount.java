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

public class WordCount {

    public static class ShingleMapper
            extends Mapper<Object, Text, Text, IntWritable>{

        private final static int minShingleSize = 2;
        private final static int maxShingleSize = 10;
        static boolean onlyLetters = false;

        private StringBuilder[] currentShingles;

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

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
                            word.set(currentShingles[j].toString());
                            context.write(word, one);
                        }
                    }
                }
            }

        }
    }

    public static class ShingleSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);

        ShingleMapper.onlyLetters = args.length >= 3 && args[2].equals("true");
        job.setMapperClass(ShingleMapper.class);

        job.setCombinerClass(ShingleSumReducer.class);
        job.setReducerClass(ShingleSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}