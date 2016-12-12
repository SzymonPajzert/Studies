package terasort.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

// TODO add setting output directory to allow having multiple results
public class ConfigurationWrap {
    public final Configuration conf;
    private final FileSystem fileSystem;
    private final int numPartitions;

    public ConfigurationWrap(Configuration conf) throws IOException {
        this.conf = conf;
        this.fileSystem = FileSystem.get(conf);
        this.numPartitions = conf.getInt("mapreduce.job.reduces", 5);
    }

    private Stream<Path> getPaths(String property) throws IOException {
        String root = conf.get("fs.default.name");
        String path = root + "/user/szymonpajzert/" + conf.get(property);
        System.out.println("Using location: " + path);

        return Arrays.asList(fileSystem.listStatus(new Path(path)))
                .stream()
                .filter(x -> x.getLen() > 0)
                .map(FileStatus::getPath);
    }

    private Path getPath(String property) throws IOException {
        return getPaths(property).findFirst().get();
    }

    public HashMap<Integer, Integer> getWindowSizes() throws IOException {
        Stream<Path> ps = getPaths("window.sizes.location");
        HashMap<Integer, Integer> result = new HashMap<>();

        for(Path p : ps.toArray(Path[]::new)) {
            FSDataInputStream reader = fileSystem.open(p);
            Scanner scanner = new Scanner(new InputStreamReader(reader));

            int partition, size;
            partition = scanner.nextInt();
            size = scanner.nextInt();
            result.put(partition, size);

            scanner.close();
            reader.close();
        }

        return result;
    }

    public int[] readSplitPoints() throws IOException {
        Path p = getPath("partition.location");

        int[] result = new int[numPartitions- 1];
        FSDataInputStream reader = fileSystem.open(p);

        Scanner scanner = new Scanner(new InputStreamReader(reader));
        for(int i = 0; i < numPartitions - 1; ++i) {
            result[i] = scanner.nextInt();
            System.out.println("Read " + result[i]);
        }

        scanner.close();
        reader.close();
        return result;
    }
}
