package com.genuitec.piplug.tools.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class IntervalProgressJob extends Job {

    private static final long INTERVAL = 500;
    private IProgressMonitor monitor;
    private long duration;

    public IntervalProgressJob(IProgressMonitor monitor, long duration) {
	super("Timer progress");
	this.duration = duration;
	setUser(false);
	setSystem(true);
	this.monitor = monitor;
    }

    @Override
    protected IStatus run(IProgressMonitor intervalJobProgress) {
	if (intervalJobProgress.isCanceled() || monitor.isCanceled())
	    return Status.OK_STATUS;
	if (duration < 0)
	    return Status.OK_STATUS;
	this.monitor.worked((int) INTERVAL);
	this.duration -= INTERVAL;
	schedule(INTERVAL);
	return Status.OK_STATUS;
    }

}
