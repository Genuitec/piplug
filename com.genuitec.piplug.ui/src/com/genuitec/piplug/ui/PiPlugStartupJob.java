package com.genuitec.piplug.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public class PiPlugStartupJob extends Job {

    private final PiPlugAppContainer container;
    private final PiPlugStartingUpComposite startup;

    public PiPlugStartupJob(PiPlugAppContainer container,
	    PiPlugStartingUpComposite startup) {
	super("Startup PiPlug");
	this.container = container;
	this.startup = startup;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
	runInUI(new UpdateMessage("Loading plug-ins..."));
	try {
	    Thread.sleep(2500);
	} catch (InterruptedException e) {
	    // ignore
	}
	runInUI(new PiPlugInitializeDashboard(container));
	return Status.OK_STATUS;
    }

    private void runInUI(Runnable runnable) {
	Display.getDefault().syncExec(runnable);
    }

    private final class UpdateMessage implements Runnable {
	private String message;

	UpdateMessage(String message) {
	    this.message = message;
	}

	@Override
	public void run() {
	    startup.setMessage(message);
	}
    }
}