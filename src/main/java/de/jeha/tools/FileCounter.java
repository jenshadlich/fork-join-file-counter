package de.jeha.tools;

import jsr166y.forkjoin.ForkJoinExecutor;
import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.RecursiveAction;
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

    /**
     * Count files.
     */
    private final class CountFilesAction extends RecursiveAction {

        private final String[] files;
        private final File folder;
        private AtomicInteger number;

        private CountFilesAction(String[] files, File folder, AtomicInteger number) {
            super();
            this.files = files;
            this.folder = folder;
            this.number = number;
        }

        @Override
        protected void compute() {
            if (files == null) {
                return;
            }

            List<RecursiveAction> tasks = new ArrayList<RecursiveAction>();
            for (String file : files) {
                tasks.add(new CountFileAction(file, folder, number));
            }

            RecursiveAction.forkJoin(tasks);
        }
    }

    /**
     * Count file.
     */
    private final class CountFileAction extends RecursiveAction {

        private final String file;
        private final File folder;
        private AtomicInteger number;

        private CountFileAction(String file, File folder, AtomicInteger number) {
            super();
            this.file = file;
            this.folder = folder;
            this.number = number;
        }

        @Override
        protected void compute() {
            File currentFile = new File(folder, file);
            if (currentFile.isDirectory()) {
                if (currentFile.canRead()) {
                    RecursiveAction.forkJoin(new RecursiveAction[]{
                            new CountFilesAction(currentFile.list(), currentFile, number)
                    });
                } else {
                    LOG.error("Can't read folder: {}", currentFile.getAbsolutePath());
                }
            } else {
                number.incrementAndGet();
            }
        }
    }

}
