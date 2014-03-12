package com.genuitec.piplug.ui;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.BundleDescriptors;
import com.genuitec.piplug.client.IPiPlugClientListener;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.ui.PiPlugDashboardComposite.PiPlugAppHandle;

public class PiPlugRuntimeServices implements IPiPlugClientListener {
    private static final Class<?>[] MARSHALLABLE_ROOTS = { BundleDescriptors.class };
    private static PiPlugRuntimeServices instance = new PiPlugRuntimeServices();
    private PiPlugClient client;
    private File storageLocation;
    private final Set<Bundle> installedBundles = new HashSet<Bundle>();
    private final Set<Bundle> startedBundles = new HashSet<Bundle>();
    private BundleDescriptors localBundleDescriptors;
    private PiPlugAppContainer container;

    // singleton
    private PiPlugRuntimeServices() {
	storageLocation = new File(System.getProperty("user.home"),
		".piplug/cache");
    }

    void setClient(PiPlugClient client) {
	this.client = client;
	client.addListener(this);
    }

    @Override
    public void newBundleList(BundleDescriptors remoteBundleDescriptors) {
	try {
	    boolean removedApps = synchronizeLocalBundles(
		    remoteBundleDescriptors, container.getStatusLine());
	    Map<BundleDescriptor, IPiPlugApplication> apps = loadLocalApps(container
		    .getStatusLine());
	    container.getHome().setApplications(apps, removedApps);
	} catch (CoreException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private PiPlugAppHandle findAppHandle(BundleDescriptor next) {
	if (container == null)
	    return null;
	return container.findAppHandle(next);
    }

    public static PiPlugRuntimeServices getInstance() {
	return instance;
    }

    public void startup(PiPlugAppContainer container,
	    PiPlugStartingUpComposite startup) {
	this.container = container;
	PiPlugStartupJob job = new PiPlugStartupJob(container, startup);
	// idle processor to allow VNC or other display actions to run smoothly
	job.schedule(500);
    }

    public void shutdown() {
	client.removeListener(this);
	try {
	    client.disconnect();
	} catch (CoreException e) {
	    e.printStackTrace();
	}

	for (Bundle next : startedBundles) {
	    try {
		next.stop();
	    } catch (Exception ignored) {
		// best effort
	    }
	}
	for (Bundle next : installedBundles) {
	    try {
		next.uninstall();
	    } catch (Exception ignored) {
		// best effort
	    }
	}
    }

    public File getPathTo(BundleDescriptor desc) {
	return new File(storageLocation, desc.getBundleID() + "_"
		+ desc.getVersion() + '_' + desc.getLastUpdatedOn().getTime()
		+ ".jar");
    }

    public File getStorageLocation() {
	return storageLocation;
    }

    public BundleDescriptors readLocalBundleDescriptors() throws JAXBException {
	Unmarshaller unmarshaller = createUnmarshaller();

	File storageLocation = PiPlugRuntimeServices.getInstance()
		.getStorageLocation();
	storageLocation.mkdirs();

	localBundleDescriptors = new BundleDescriptors();
	localBundleDescriptors
		.setDescriptors(new ArrayList<BundleDescriptor>());
	File localBundlesFile = getLocalBundlesFile(storageLocation);
	if (localBundlesFile.isFile()) {
	    try {
		localBundleDescriptors = (BundleDescriptors) unmarshaller
			.unmarshal(localBundlesFile);
		List<BundleDescriptor> localBundlesList = localBundleDescriptors
			.getDescriptors();
		Iterator<BundleDescriptor> iter = localBundlesList.iterator();
		while (iter.hasNext()) {
		    BundleDescriptor next = iter.next();
		    if (!PiPlugRuntimeServices.getInstance().getPathTo(next)
			    .isFile())
			iter.remove();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return localBundleDescriptors;
    }

    private File getLocalBundlesFile(File storageLocation) {
	return new File(storageLocation, "bundles.xml");
    }

    private Unmarshaller createUnmarshaller() throws JAXBException {
	JAXBContext jaxb = JAXBContext.newInstance(MARSHALLABLE_ROOTS);
	return jaxb.createUnmarshaller();
    }

    private Marshaller createMarshaller() throws JAXBException {
	JAXBContext jaxb = JAXBContext.newInstance(MARSHALLABLE_ROOTS);
	return jaxb.createMarshaller();
    }

    public void saveBundleDescriptors(BundleDescriptors bundleDescriptors)
	    throws CoreException {
	File localBundlesFile = getLocalBundlesFile(storageLocation);

	try {
	    Marshaller marshaller = createMarshaller();
	    marshaller.marshal(bundleDescriptors, localBundlesFile);
	} catch (Exception e) {
	    throw new CoreException(new Status(IStatus.ERROR,
		    PiPlugUIActivator.PLUGIN_ID,
		    "Could not read local bundles file", e));
	}
    }

    public boolean synchronizeLocalBundles(
	    BundleDescriptors remoteBundleDescriptors, IStatusLine statusLine)
	    throws CoreException {

	if (remoteBundleDescriptors.equals(localBundleDescriptors)) {
	    this.localBundleDescriptors = remoteBundleDescriptors;
	    return false;
	}

	// First unload existing running apps that will change
	List<BundleDescriptor> toRemove = new ArrayList<BundleDescriptor>(
		localBundleDescriptors.getDescriptors());
	toRemove.removeAll(remoteBundleDescriptors.getDescriptors());

	boolean removedApps = false;
	if (!toRemove.isEmpty()) {
	    statusLine.updateMessage("Cleaning up old plug-ins...");
	    for (BundleDescriptor next : toRemove) {
		PiPlugAppHandle appHandle = findAppHandle(next);
		if (null != appHandle) {
		    boolean dispose = remoteBundleDescriptors.matchesByID(next)
			    .isEmpty();
		    removedApps |= dispose;
		    appHandle.unloadExisting(dispose);
		}
	    }
	    for (BundleDescriptor next : toRemove) {
		Bundle bundle = findBundle(next, startedBundles);
		if (null != bundle) {
		    try {
			bundle.stop();
			startedBundles.remove(bundle);
		    } catch (Throwable t) {
			t.printStackTrace();
		    }
		}
	    }
	    for (BundleDescriptor next : toRemove) {
		Bundle bundle = findBundle(next, installedBundles);
		if (null != bundle) {
		    try {
			bundle.uninstall();
			installedBundles.remove(bundle);
		    } catch (Throwable t) {
			t.printStackTrace();
		    }
		}
	    }
	    for (BundleDescriptor next : toRemove) {
		File path = getPathTo(next);
		if (!path.delete()) {
		    path.deleteOnExit();
		}
	    }
	}

	List<BundleDescriptor> toDownload = new ArrayList<BundleDescriptor>(
		remoteBundleDescriptors.getDescriptors());
	toDownload.removeAll(localBundleDescriptors.getDescriptors());

	for (BundleDescriptor next : toDownload) {
	    statusLine.updateMessage("Downloading " + next.getBundleID()
		    + "...");
	    File target = PiPlugRuntimeServices.getInstance().getPathTo(next);
	    client.downloadBundle(next, target);
	}

	statusLine.updateMessage("Saving plug-ins list...");
	PiPlugRuntimeServices.getInstance().saveBundleDescriptors(
		remoteBundleDescriptors);

	this.localBundleDescriptors = remoteBundleDescriptors;
	return removedApps;
    }

    private Bundle findBundle(BundleDescriptor bundle, Set<Bundle> bundles) {
	for (Bundle next : bundles) {
	    if (next.getSymbolicName().equals(bundle.getBundleID())
		    && next.getVersion().toString().equals(bundle.getVersion()))
		return next;
	}
	return null;
    }

    public void startInstalledBundles() {
	if (installedBundles == null)
	    return;
	for (Bundle bundle : installedBundles) {
	    try {
		if (Bundle.ACTIVE != bundle.getState()) {
		    bundle.start(Bundle.START_TRANSIENT);
		    startedBundles.add(bundle);
		    System.out.println("Successfully activated bundle "
			    + bundle.getSymbolicName() + " from "
			    + bundle.getVersion());
		}
	    } catch (Throwable t) {
		// TODO log bundle activation failure
		System.out.println("Unable to activate bundle "
			+ bundle.getSymbolicName());
		t.printStackTrace();
	    }
	}
    }

    public void installLocalBundles() {
	BundleContext context = PiPlugUIActivator.getContext();
	descriptors: for (BundleDescriptor next : localBundleDescriptors
		.getDescriptors()) {
	    // To make this re-entrant, if we already have installed a bundle
	    // don't try to install it again.
	    for (Bundle nextBundle : installedBundles) {
		if (next.getBundleID().equals(nextBundle.getSymbolicName())
			&& next.getVersion().equals(
				nextBundle.getVersion().toString())) {
		    continue descriptors;
		}
	    }

	    Bundle existing = Platform.getBundle(next.getBundleID());
	    if (existing != null) {
		try {
		    existing.uninstall();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    String path = URIUtil.toUnencodedString(PiPlugRuntimeServices
		    .getInstance().getPathTo(next).toURI());
	    try {
		installedBundles
			.add(context.installBundle("reference:" + path));
	    } catch (Exception e) {
		// TODO log bundle installation failure
		e.printStackTrace();
	    }
	}
    }

    public Map<BundleDescriptor, IPiPlugApplication> loadLocalApps(
	    IStatusLine statusLine) {
	Map<BundleDescriptor, IPiPlugApplication> applications = new HashMap<BundleDescriptor, IPiPlugApplication>();

	statusLine.updateMessage("Loading plug-ins...");
	installLocalBundles();

	statusLine.updateMessage("Activating plug-ins...");
	startInstalledBundles();

	statusLine.updateMessage("Detecting application plug-ins...");
	for (Bundle next : installedBundles) {
	    IPiPlugApplication app = loadAppFrom(next.getSymbolicName());
	    if (app != null)
		applications.put(getBundleDescriptor(next), app);
	}

	return applications;
    }

    private BundleDescriptor getBundleDescriptor(Bundle bundle) {
	if (bundle == null)
	    return null;
	List<BundleDescriptor> descriptors = localBundleDescriptors
		.getDescriptors();
	String bundleID = bundle.getSymbolicName();
	String bundleVersion = bundle.getVersion().toString();
	for (BundleDescriptor next : descriptors) {
	    if (bundleID.equals(next.getBundleID())
		    && bundleVersion.equals(next.getVersion()))
		return next;
	}
	return null;
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

    public Map<BundleDescriptor, IPiPlugApplication> connect(
	    IStatusLine statusLine) throws CoreException {
	statusLine.updateMessage("Discovering plug-in server...");
	InetSocketAddress serverAt = client.discoverServer(30000);

	statusLine.updateMessage("Connecting to plug-in server...");
	client.connectTo(serverAt);

	statusLine.updateMessage("Retrieving plug-in list...");
	BundleDescriptors remoteBundleDescriptors = client
		.getBundlesFromCache();

	PiPlugRuntimeServices.getInstance().synchronizeLocalBundles(
		remoteBundleDescriptors, statusLine);

	return PiPlugRuntimeServices.getInstance().loadLocalApps(statusLine);
    }

}
