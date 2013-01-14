package de.jeha.tools.actions;

import jsr166y.forkjoin.RecursiveAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Count files.
 *
 * @author jenshadlich@googlemail.com
 */
public final class CountFilesAction extends AbstractFileCounterAction {

    private final String[] files;

    public CountFilesAction(String[] files, File folder, AtomicInteger number) {
        super(folder, number);
        this.files = files;
    }

    @Override
    protected void compute() {
        if (files == null) {
            return;
        }

        List<RecursiveAction> tasks = new ArrayList<RecursiveAction>();
        for (String file : files) {
            tasks.add(new CountFileAction(file, getFolder(), getNumber()));
        }

        RecursiveAction.forkJoin(tasks);
    }
}
