package de.jeha.tools.actions;

import jsr166y.forkjoin.RecursiveAction;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jenshadlich@googlemail.com
 */
public abstract class AbstractFileCounterAction extends RecursiveAction {

    private final File folder;
    private final AtomicInteger number;

    public AbstractFileCounterAction(File folder, AtomicInteger number) {
        this.folder = folder;
        this.number = number;
    }

    public File getFolder() {
        return folder;
    }

    public AtomicInteger getNumber() {
        return number;
    }

}
