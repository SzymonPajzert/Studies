package terasort;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import terasort.mapper.IntMapper;
import terasort.mapper.RemoteRelevanceMapper;
import terasort.mapper.SamplingIntMapper;
import terasort.partitioner.InputPartitioner;
import terasort.reducer.*;
import terasort.util.PairInt;


public class SlidingAggregation {

    /** Samples available data to one reducer, the reducer then writes data in splitPointsPath.
     *
     * @param conf Configuration of the mapreduce task
     * @param inputPath Path where data is available
     * @return true if job execution is successful
     * @throws Exception
     */
    private static boolean createSplitPoints(Configuration conf, Path inputPath) throws Exception {
        Job job = Job.getInstance(conf, "Split points creation");

        job.setJarByClass(SlidingAggregation.class);

        job.setMapperClass(SamplingIntMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(SplitPointsReducer.class);
        // job.setNumReduceTasks(1);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(conf.get("partition.location")));

        return job.waitForCompletion(true);
    }

    private static boolean countWindowSize(Configuration conf, Path inputPath) throws Exception {
        Job job = Job.getInstance(conf, "Count Window Size");

        job.setJarByClass(SlidingAggregation.class);

        job.setMapperClass(IntMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setPartitionerClass(InputPartitioner.class);

        job.setReducerClass(RankingCounter.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(conf.get("window.sizes.location")));

        return job.waitForCompletion(true);
    }

    private static boolean countRanking(Configuration conf, Path inputPath) throws Exception {
        Job job = Job.getInstance(conf, "Count Ranking");

        job.setJarByClass(SlidingAggregation.class);

        job.setMapperClass(IntMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setPartitionerClass(InputPartitioner.class);

        job.setReducerClass(RankingReducer.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(conf.get("ranking.result")));

        return job.waitForCompletion(true);
    }

    private static boolean aggregateWindows(Configuration conf) throws Exception {
        Job job = Job.getInstance(conf, "Aggregate windows");

        job.setJarByClass(SlidingAggregation.class);

        RemoteRelevanceMapper.setSendRemotelyRelevant(false);
        job.setMapperClass(RemoteRelevanceMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(PairInt.class);

        job.setPartitionerClass(InputPartitioner.class);

        job.setReducerClass(SumReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(conf.get("ranking.result")));
        FileOutputFormat.setOutputPath(job, new Path(conf.get("windows.aggregate")));

        return job.waitForCompletion(true);
    }

    private static boolean slidingAggregation(Configuration conf, Path outputPath) throws Exception {
        Job job = Job.getInstance(conf, "Sliding aggregation");

        job.setJarByClass(SlidingAggregation.class);

        RemoteRelevanceMapper.setSendRemotelyRelevant(true);

        job.setMapperClass(RemoteRelevanceMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(PairInt.class);

        job.setPartitionerClass(InputPartitioner.class);



        job.setReducerClass(SlidingAggregationReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(conf.get("ranking.result")));
        FileOutputFormat.setOutputPath(job, outputPath);

        return job.waitForCompletion(true);
    }

    public static void main(String[] args) throws Exception {
        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);
        int windowSize = Integer.parseInt(args[2]);

        Configuration conf = new Configuration();
        conf.setInt("window.size", windowSize);

        // createSplitPoints(conf, inputPath);
        // countWindowSize(conf, inputPath);
        // countRanking(conf, inputPath);
        // aggregateWindows(conf);
        slidingAggregation(conf, outputPath);
    }
}
