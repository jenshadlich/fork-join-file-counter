package de.jeha.tools;

import de.jeha.tools.actions.CountFilesAction;
import jsr166y.forkjoin.ForkJoinExecutor;
import jsr166y.forkjoin.ForkJoinPool;
import org.apache.commons.lang3.time.StopWatch;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jenshadlich@googlemail.com
 */
public class FileCounter {

    private static final Logger LOG = LoggerFactory.getLogger(FileCounter.class);

    private String rootDirectory;

    @Option(name = "-t", usage = "number of threads")
    private final Integer threads = 1;

    @Argument
    private final List<String> arguments = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        new FileCounter().doMain(args);
    }

    private void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            if (arguments.isEmpty()) {
                throw new CmdLineException(parser, "No arguments are given");
            }
            rootDirectory = arguments.get(0);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar fork-join-file-counter.jar [options...] <root directory>");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        System.out.printf("Number of threads = %d\n", threads);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int numberOfFiles = countFiles();
        stopWatch.stop();

        System.out.printf("Number of files = %d\n", numberOfFiles);
        System.out.printf("Duration = %dms\n", stopWatch.getTime());
    }

    private int countFiles() throws IOException {
        final ForkJoinExecutor threadPool = new ForkJoinPool(threads);
        final File root = new File(rootDirectory);
        final AtomicInteger result = new AtomicInteger(0);
        if (root.exists() && root.canRead()) {
            threadPool.invoke(new CountFilesAction(root.list(), root, result));
        } else {
            LOG.warn("Directory does not exist: {}", rootDirectory);
        }

        return result.intValue();
    }

}
