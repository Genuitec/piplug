package com.genuitec.piplug.ui;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.common.BundleDescriptor;
import com.genuitec.piplug.common.BundleDescriptors;

public class PiPlugStartupJob extends Job {

    private final PiPlugAppContainer container;
    private final PiPlugStartingUpComposite startup;
    private PiPlugClient client;
    private File storageLocation;

    public PiPlugStartupJob(PiPlugAppContainer container,
	    PiPlugStartingUpComposite startup) {
	super("Startup PiPlug");
	this.container = container;
	this.startup = startup;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

	Marshaller marshaller;
	Unmarshaller unmarshaller;

	try {
	    JAXBContext jaxb = JAXBContext.newInstance(BundleDescriptors.class);
	    marshaller = jaxb.createMarshaller();
	    unmarshaller = jaxb.createUnmarshaller();
	} catch (Exception e) {
	    return new Status(IStatus.ERROR, "com.genuitec.piplug.ui",
		    "Unable to prepare JAXB context", e);
	}

	storageLocation = new File(System.getProperty("user.home"),
		".piplug/cache");
	storageLocation.mkdirs();

	List<BundleDescriptor> localBundlesList = null;
	File localBundlesFile = new File(storageLocation, "bundles.xml");
	if (localBundlesFile.isFile()) {
	    try {
		BundleDescriptors descriptors = (BundleDescriptors) unmarshaller
			.unmarshal(localBundlesFile);
		localBundlesList = descriptors.getDescriptors();

		Iterator<BundleDescriptor> iter = localBundlesList.iterator();
		while (iter.hasNext()) {
		    BundleDescriptor next = iter.next();
		    if (!getPathTo(next).isFile())
			iter.remove();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	if (localBundlesList == null)
	    localBundlesList = new ArrayList<BundleDescriptor>();

	client = new PiPlugClient();

	try {
	    runInUI(new UpdateMessage("Discovering plug-in server..."));
	    InetSocketAddress serverAt = client.discoverServer(30000);

	    runInUI(new UpdateMessage("Connecting to plug-in server..."));
	    client.connectTo(serverAt);

	    runInUI(new UpdateMessage("Retrieving plug-in list..."));
	    List<BundleDescriptor> remoteBundlesList = client.listBundles();

	    if (!remoteBundlesList.equals(localBundlesList)) {

		List<BundleDescriptor> toDownload = new ArrayList<BundleDescriptor>(
			remoteBundlesList);
		toDownload.removeAll(localBundlesList);

		for (BundleDescriptor next : toDownload) {
		    runInUI(new UpdateMessage("Downloading "
			    + next.getBundleID() + "..."));
		    File target = getPathTo(next);
		    client.downloadBundle(next, target);
		}

		List<BundleDescriptor> toRemove = new ArrayList<BundleDescriptor>(
			localBundlesList);
		toRemove.removeAll(remoteBundlesList);

		if (!toRemove.isEmpty()) {
		    runInUI(new UpdateMessage("Cleaning up old plug-ins..."));
		    for (BundleDescriptor next : toRemove)
			getPathTo(next).delete();
		}

		runInUI(new UpdateMessage("Saving plug-ins list..."));
		BundleDescriptors newBundles = new BundleDescriptors();
		newBundles.setDescriptors(remoteBundlesList);
		marshaller.marshal(newBundles, localBundlesFile);
		localBundlesList = remoteBundlesList;

	    }

	    runInUI(new UpdateMessage("Loading plug-ins..."));

	} catch (Exception e) {

	    e.printStackTrace();
	    if (e instanceof CoreException) {
		CoreException ce = (CoreException) e;
		runInUI(new UpdateMessage("Failure: "
			+ ce.getStatus().getMessage()));
	    } else {
		runInUI(new UpdateMessage("Failure: " + e.getMessage()));
	    }
	    try {
		Thread.sleep(2500);
	    } catch (InterruptedException ignored) {
	    }
	    runInUI(new UpdateMessage("Loading cached plug-ins..."));

	}

	List<Bundle> loadedBundles = new ArrayList<Bundle>();
	BundleContext context = PiPlugUIActivator.getContext();
	for (BundleDescriptor next : localBundlesList) {
	    Bundle existing = Platform.getBundle(next.getBundleID());
	    if (existing != null) {
		if ("qualifier".equals(existing.getVersion().getQualifier())) {
		    System.out.println("Skipping activation of bundle "
			    + next.getBundleID() + " " + next.getVersion()
			    + " as a development version was detected");
		    continue;
		}
		try {
		    existing.uninstall();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    String path = URIUtil.toUnencodedString(getPathTo(next).toURI());
	    try {
		loadedBundles.add(context.installBundle("reference:" + path));
	    } catch (Exception e) {
		// TODO log bundle installation failure
		e.printStackTrace();
	    }
	}

	runInUI(new UpdateMessage("Activating plug-ins..."));

	for (Bundle bundle : loadedBundles) {
	    try {
		bundle.start(Bundle.START_TRANSIENT);
		System.out.println("Successfully activated bundle "
			+ bundle.getSymbolicName() + " from "
			+ bundle.getVersion());
	    } catch (Throwable t) {
		// TODO log bundle activation failure
		System.out.println("Unable to activate bundle "
			+ bundle.getSymbolicName());
		t.printStackTrace();
	    }
	}

	runInUI(new UpdateMessage("Detecting application plug-ins..."));

	Set<IPiPlugApplication> applications = new TreeSet<IPiPlugApplication>(
		new PiPlugAppComparator());
	for (BundleDescriptor next : localBundlesList) {
	    IPiPlugApplication app = loadAppFrom(next.getBundleID());
	    if (app != null)
		applications.add(app);
	}

	runInUI(new PiPlugInitializeDashboard(applications, container,
		loadedBundles, client));
	return Status.OK_STATUS;
    }

    private File getPathTo(BundleDescriptor desc) {
	return new File(storageLocation, desc.getBundleID() + "_"
		+ desc.getVersion() + ".jar");
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