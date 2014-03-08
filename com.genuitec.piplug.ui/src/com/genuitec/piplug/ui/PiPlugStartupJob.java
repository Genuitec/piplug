package com.genuitec.piplug.ui;

import java.util.Map;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;

public class PiPlugStartupJob extends Job implements IStatusLine {

    private final PiPlugAppContainer container;
    private final PiPlugStartingUpComposite startup;
    private PiPlugClient client;

    public PiPlugStartupJob(PiPlugAppContainer container,
	    PiPlugStartingUpComposite startup) {
	super("Startup PiPlug");
	this.container = container;
	this.startup = startup;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

	try {
	    PiPlugRuntimeServices.getInstance().readLocalBundleDescriptors();
	} catch (JAXBException e) {
	    return new Status(IStatus.ERROR, "com.genuitec.piplug.ui",
		    "Unable to prepare JAXB context", e);
	}

	client = new PiPlugClient();
	PiPlugRuntimeServices.getInstance().setClient(client);

	Map<BundleDescriptor, IPiPlugApplication> applications;
	try {
	    applications = PiPlugRuntimeServices.getInstance().connect(this);
	} catch (Exception e) {
	    new Job("Connect") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
		    try {
			PiPlugRuntimeServices.getInstance().connect(
				PiPlugStartupJob.this);
		    } catch (Exception e) {
			e.printStackTrace();
			schedule(30000);
		    }
		    return Status.OK_STATUS;
		}
	    }.schedule(30000);
	    e.printStackTrace();
	    if (e instanceof CoreException) {
		CoreException ce = (CoreException) e;
		updateMessage("Failure: " + ce.getStatus().getMessage());
	    } else {
		updateMessage("Failure: " + e.getMessage());
	    }
	    try {
		Thread.sleep(2500);
	    } catch (InterruptedException ignored) {
	    }
	    updateMessage("Loading cached plug-ins...");
	    applications = PiPlugRuntimeServices.getInstance().loadLocalApps(
		    this);
	}

	runInUI(new PiPlugInitializeDashboard(applications, container, client));
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

    @Override
    public void updateMessage(String message) {
	runInUI(new UpdateMessage(message));
    }
}