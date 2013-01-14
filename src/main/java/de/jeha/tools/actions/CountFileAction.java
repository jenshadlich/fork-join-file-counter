package de.jeha.tools.actions;

import jsr166y.forkjoin.RecursiveAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Count file.
 *
 * @author jenshadlich@googlemail.com
 */
public final class CountFileAction extends AbstractFileCounterAction {

    private static final Logger LOG = LoggerFactory.getLogger(CountFileAction.class);

    private final String file;

    public CountFileAction(String file, File folder, AtomicInteger number) {
        super(folder, number);
        this.file = file;
    }

    @Override
    protected void compute() {
        File currentFile = new File(getFolder(), file);
        if (currentFile.isDirectory()) {
            if (currentFile.canRead()) {
                RecursiveAction.forkJoin(new RecursiveAction[]{
                        new CountFilesAction(currentFile.list(), currentFile, getNumber())
                });
            } else {
                LOG.error("Can't read folder: {}", currentFile.getAbsolutePath());
            }
        } else {
            getNumber().incrementAndGet();
        }
    }
}
