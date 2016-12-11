package terasort.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class ConfigurationWrap {
    public final Configuration conf;
    public final FileSystem fileSystem;
    public final int numPartitions;

    public ConfigurationWrap(Configuration conf) throws IOException {
        this.conf = conf;
        this.fileSystem = FileSystem.get(conf);
        this.numPartitions = conf.getInt("mapreduce.job.reduces", 5);
    }

    public Stream<Path> getPaths(String property) throws IOException {
        String root = conf.get("fs.default.name");
        String path = root + "/user/szymonpajzert/" + conf.get(property);
        System.out.println("Using location: " + path);

        return Arrays.asList(fileSystem.listStatus(new Path(path)))
                .stream()
                .filter(x -> x.getLen() > 0)
                .map(FileStatus::getPath);
    }

    public Path getPath(String property) throws IOException {
        return getPaths(property).findFirst().get();
    }
}
