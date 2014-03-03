package com.genuitec.piplug.app.clock;

import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public class ClockJob extends Job implements Runnable {

    private boolean running = true;
    private ClockComposite composite;
    private Calendar calendar;

    public ClockJob(ClockComposite composite) {
	super("clock");
	this.composite = composite;
	this.calendar = Calendar.getInstance();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
	Display.getDefault().syncExec(this);
	if (running) {
	    calendar.setTimeInMillis(System.currentTimeMillis());
	    int milli = calendar.get(Calendar.MILLISECOND);
	    schedule(1000 - milli);
	}
	return Status.OK_STATUS;
    }

    public void stop() {
	running = false;
    }

    public void run() {
	if (composite.isDisposed()) {
	    running = false;
	    return;
	}
	composite.updateTime();
    }
}