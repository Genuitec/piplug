package com.genuitec.piplug.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import com.genuitec.piplug.api.IPiPlugApplication;

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
	    bundleIDs.add("com.genuitec.piplug.app.infocom");
	    bundleIDs.add("com.genuitec.piplug.app.infocom.zork1");
	    bundleIDs.add("com.genuitec.piplug.app.infocom.zork2");
	    bundleIDs.add("com.genuitec.piplug.app.infocom.zork3");
	    Thread.sleep(500);
	} catch (InterruptedException e) {
	    // ignore
	}

	Set<IPiPlugApplication> applications = new TreeSet<IPiPlugApplication>(
		new PiPlugAppComparator());
	for (String bundleID : bundleIDs) {
	    IPiPlugApplication app = loadAppFrom(bundleID);
	    if (app != null)
		applications.add(app);
	}

	runInUI(new PiPlugInitializeDashboard(applications, container));
	return Status.OK_STATUS;
    }

    private IPiPlugApplication loadAppFrom(String bundleID) {
	Bundle bundle = Platform.getBundle(bundleID);
	if (bundle == null) {
	    System.err.println("Unable to load bundle: " + bundleID);
	    return null;
	}
	IConfigurationElement[] ces = Platform.getExtensionRegistry()
		.getConfigurationElementsFor("com.genuitec.piplug.api", "app");
	for (IConfigurationElement next : ces) {
	    String contributor = next.getContributor().getName();
	    if (contributor.equals(bundleID)) {
		try {
		    return (IPiPlugApplication) next
			    .createExecutableExtension("class");
		} catch (Exception e) {
		    System.err.println("Unable to instantiate class: " + e);
		}
	    }
	}
	return null;
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