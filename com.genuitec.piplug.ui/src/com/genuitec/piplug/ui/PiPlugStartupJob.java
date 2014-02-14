package com.genuitec.piplug.ui;

import java.util.ArrayList;
import java.util.List;

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

	// TODO: Contact server for list of bundles
	runInUI(new UpdateMessage("Retrieving plug-in list..."));
	// later to dynamically get bundle list, for now, we'll hard code the
	// list of plugins
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    // ignore
	}
	// TODO: Download bundles from remote server
	runInUI(new UpdateMessage("Downloading plug-ins..."));
	try {
	    Thread.sleep(1500);
	} catch (InterruptedException e) {
	    // ignore
	}
	// TODO: Dynamically activate bundles in runtime
	runInUI(new UpdateMessage("Loading plug-ins..."));
	List<String> bundleIDs = new ArrayList<String>();
	try {
	    bundleIDs.add("com.genuitec.piplug.app.clock");
	    bundleIDs.add("com.genuitec.piplug.app.clock");
	    Thread.sleep(500);
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