package terasort;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import terasort.mapper.IntMapper;
import terasort.mapper.SamplingIntMapper;
import terasort.partitioner.InputPartitioner;
import terasort.reducer.RankingReducer;
import terasort.reducer.SplitPointsReducer;


public class TeraSort {
    private static Path splitPointsPath = new Path("_partition.lst");

    /** Samples available data to one reducer, the reducer then writes data in splitPointsPath.
     *
     * @param conf Configuration of the mapreduce task
     * @param inputPath Path where data is available
     * @return true if job execution is successful
     * @throws Exception
     */
    private static boolean createSplitPoints(Configuration conf, Path inputPath) throws Exception {
        Job job = Job.getInstance(conf, "Split points creation");

        job.setJarByClass(TeraSort.class);

        job.setMapperClass(SamplingIntMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(IntWritable.class);

        job.setReducerClass(SplitPointsReducer.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, splitPointsPath);

        return job.waitForCompletion(true);
    }

    private static boolean makeTeraSort(Configuration conf, Path inputPath, Path outputPath) throws Exception {
        int reduces = conf.getInt("mapreduce.job.reduces", 1);
        InputPartitioner.setSplitPointsPath(splitPointsPath);

        Job job = Job.getInstance(conf, "TeraSort");
        job.setJarByClass(TeraSort.class);
        job.setMapperClass(IntMapper.class);
        job.setPartitionerClass(InputPartitioner.class);
        job.setReducerClass(RankingReducer.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        return job.waitForCompletion(true);
    }

    public static void main(String[] args) throws Exception {
        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        Configuration conf = new Configuration();

        createSplitPoints(conf, inputPath);
        // makeTeraSort(conf, inputPath, outputPath);

    }
}
