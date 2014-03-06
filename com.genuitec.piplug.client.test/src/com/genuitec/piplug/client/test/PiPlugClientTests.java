package com.genuitec.piplug.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.BundleEvent;
import com.genuitec.piplug.client.BundleEventType;
import com.genuitec.piplug.client.IPiPlugClientListener;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.daemon.PiPlugDaemon;

public class PiPlugClientTests {

    private static final int LISTENER_TIMEOUT = 10000;

    public class TestListener implements IPiPlugClientListener {

	private List<BundleEvent> events;

	public void waitFor(BundleEventType type, long timeout,
		BundleDescriptor... descriptors) throws TimeoutException {
	    if (gotMatchingEvents(type, descriptors))
		return;
	    synchronized (this) {
		long end = System.currentTimeMillis() + timeout;
		while (!gotMatchingEvents(type, descriptors)) {
		    long waitTime = end - System.currentTimeMillis();
		    if (waitTime <= 0)
			throw new TimeoutException();
		    try {
			wait(waitTime);
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
	    }
	}

	private boolean gotMatchingEvents(BundleEventType type,
		BundleDescriptor[] descriptors) {
	    if (events == null || events.isEmpty())
		return false;
	    if (descriptors == null || descriptors.length == 0)
		return false;

	    Set<BundleDescriptor> toMatch = new HashSet<BundleDescriptor>(
		    Arrays.asList(descriptors));
	    for (BundleEvent event : events) {
		if (event.getType() != type)
		    continue;
		BundleDescriptor descriptor = event.getDescriptor();

		// BundleDescriptor#equals() includes the dates
		// so Set math doesn't work here
		BundleDescriptor found = null;
		for (BundleDescriptor next : toMatch) {
		    if (descriptor.matchesIDVersion(next)) {
			found = next;
			break;
		    }
		}
		if (null != found)
		    toMatch.remove(found);
	    }
	    return toMatch.isEmpty();
	}

	@Override
	public void handleEvents(List<BundleEvent> events) {
	    synchronized (this) {
		this.events = events;
		notifyAll();
	    }
	}

    }

    private static PiPlugDaemon daemon;
    private static File storageLocation;
    private static InetSocketAddress directAt;
    private static String testBundleID = "com.genuitec.piplug.app.clock";
    private static String testBundleVersion = "1.0.0.201403041652";
    private static String testBundle2ID = "com.genuitec.piplug.app.snake";
    private static String testBundle2Version = "1.0.0";

    @BeforeClass
    public static void startDaemon() throws Exception {
	storageLocation = File.createTempFile("piplug", "test");
	storageLocation.delete();
	storageLocation.mkdirs();
	boolean debug = "true".equals(System.getProperty("daemon.debug"));
	daemon = new PiPlugDaemon(storageLocation, debug);
	daemon.start();
	directAt = new InetSocketAddress("127.0.0.1", PiPlugDaemon.DAEMON_PORT);
    }

    /**
     * Test that will:
     * 
     * 1. Discover the location of the server.
     * 
     * 2. Confirm that the server can be connected to.
     * 
     * @throws CoreException
     */
    @Test
    public void discoverAndConnect() throws CoreException {
	PiPlugClient client = new PiPlugClient();
	try {
	    InetSocketAddress discoveredAt = client.discoverServer(30000);
	    assertNotNull("server discovered", discoveredAt);
	    assertEquals("discovered port", PiPlugDaemon.DAEMON_PORT,
		    discoveredAt.getPort());
	    client.connectTo(discoveredAt);
	} finally {
	    client.disconnect();
	}
    }

    /**
     * Test that will:
     * 
     * 1. Validate no bundles are in the daemon
     * 
     * 2. Upload a bundle to the daemon
     * 
     * 3. Validate bundle list includes the new bundle
     * 
     * 4. Download the bundle
     * 
     * 5. Validate bundle size matches
     * 
     * 6. Remove the bundle
     * 
     * 7. Validate it is removed from the bundle list
     * 
     * @throws Exception
     */
    @Test
    public void uploadDownloadAndRemoveBundle() throws Exception {
	PiPlugClient client = new PiPlugClient();
	client.connectTo(directAt);
	try {

	    List<BundleDescriptor> listBundles = client.listBundles();
	    assertNotNull("bundles listing is null", listBundles);
	    assertEquals("no bundles on start", 0, listBundles.size());

	    File sourceFile = getBundle(testBundleID, testBundleVersion);

	    BundleDescriptor newDescriptor;
	    newDescriptor = new BundleDescriptor();
	    newDescriptor.setBundleID(testBundleID);
	    newDescriptor.setVersion(Version.parseVersion(testBundleVersion));

	    client.uploadBundle(newDescriptor, sourceFile);

	    listBundles = client.listBundles();
	    assertNotNull("bundles listing is null", listBundles);
	    assertEquals("bundle exists after upload", 1, listBundles.size());

	    try {

		BundleDescriptor matchedDescriptor = null;
		for (BundleDescriptor next : listBundles) {
		    if (next.matchesIDVersion(newDescriptor)) {
			matchedDescriptor = next;
			break;
		    }
		}
		assertNotNull("found match for descriptor", matchedDescriptor);
		assertNotNull("last updated not set",
			matchedDescriptor.getLastUpdatedOn());
		assertNotNull("first added not set",
			matchedDescriptor.getFirstAdded());

		File tempFile = File.createTempFile(testBundleID, ".tmp");
		tempFile.deleteOnExit();
		try {

		    client.downloadBundle(matchedDescriptor, tempFile);

		    assertEquals("file sizes match", sourceFile.length(),
			    tempFile.length());

		} finally {
		    tempFile.delete();
		}

	    } catch (Exception e) {

		// if we get an error during download test, still clean up the
		// descriptor but don't do validation
		try {
		    client.removeBundle(newDescriptor);
		} catch (Exception ignored) {
		}

		throw e;
	    }

	    client.removeBundle(newDescriptor);

	    listBundles = client.listBundles();
	    assertNotNull("bundles listing", listBundles);
	    assertEquals("bundle removed after removal", 0, listBundles.size());

	} finally {
	    client.disconnect();
	}
    }

    /**
     * Test that will:
     * 
     * 1. Register a client listener
     * 
     * 2. Upload a bundle to the daemon
     * 
     * 3. Validate that we get an ADDED event
     * 
     * 4. Upload another version of the bundle to the daemon
     * 
     * 3. Validate that we get a CHANGED event
     * 
     * 4. Remove the bundle
     * 
     * 5. Validate that we get an REMOVED event
     * 
     * @throws Exception
     */
    @Test
    public void listenForChanges() throws Exception {
	PiPlugClient client = new PiPlugClient();
	client.connectTo(directAt);
	try {
	    TestListener testListener = new TestListener();
	    client.addListener(testListener);

	    File sourceFile = getBundle(testBundleID, testBundleVersion);

	    BundleDescriptor descriptor = null;
	    BundleDescriptor newDescriptor = null;

	    descriptor = new BundleDescriptor();
	    descriptor.setBundleID(testBundleID);
	    descriptor.setVersion(Version.parseVersion(testBundleVersion));

	    try {
		client.uploadBundle(descriptor, sourceFile);

		testListener.waitFor(BundleEventType.ADDED, LISTENER_TIMEOUT,
			descriptor);
		System.out.println("Got addition");

		newDescriptor = new BundleDescriptor();
		newDescriptor.setBundleID(testBundleID);
		newDescriptor.setVersion(Version.parseVersion(testBundleVersion
			+ "1"));

		client.uploadBundle(newDescriptor, sourceFile);
		descriptor = null;

		testListener.waitFor(BundleEventType.CHANGED, LISTENER_TIMEOUT,
			newDescriptor);
		System.out.println("Got changed");

		BundleDescriptor waitForDescriptor = newDescriptor;
		client.removeBundle(newDescriptor);
		// Null this out so we don't try to remove it again
		newDescriptor = null;

		testListener.waitFor(BundleEventType.REMOVED, LISTENER_TIMEOUT,
			waitForDescriptor);
		System.out.println("Got removed");

	    } finally {
		// Do our best to clean up the daemon
		if (descriptor != null)
		    client.removeBundle(descriptor);
		if (newDescriptor != null)
		    client.removeBundle(newDescriptor);
	    }
	} finally {
	    client.disconnect();
	}
    }

    /**
     * Test that will:
     * 
     * 1. Register a client listener
     * 
     * 2. Upload two bundles in succession
     * 
     * 3. Validate that we get one batch of events with all the additions
     * 
     * @throws Exception
     */
    @Test
    public void listenForBatchedChanges() throws Exception {
	PiPlugClient client = new PiPlugClient();
	client.connectTo(directAt);
	try {
	    TestListener testListener = new TestListener();
	    client.addListener(testListener);

	    File sourceFile1 = getBundle(testBundleID, testBundleVersion);
	    File sourceFile2 = getBundle(testBundle2ID, testBundle2Version);

	    BundleDescriptor descriptor1 = null;
	    BundleDescriptor descriptor2 = null;

	    descriptor1 = new BundleDescriptor();
	    descriptor1.setBundleID(testBundleID);
	    descriptor1.setVersion(Version.parseVersion(testBundleVersion));

	    descriptor2 = new BundleDescriptor();
	    descriptor2.setBundleID(testBundle2ID);
	    descriptor2.setVersion(Version.parseVersion(testBundle2Version));

	    try {
		client.uploadBundle(descriptor1, sourceFile1);
		client.uploadBundle(descriptor2, sourceFile2);

		testListener.waitFor(BundleEventType.ADDED, LISTENER_TIMEOUT,
			descriptor1, descriptor2);
	    } finally {
		// Do our best to clean up the daemon
		if (descriptor1 != null)
		    client.removeBundle(descriptor1);
		if (descriptor2 != null)
		    client.removeBundle(descriptor2);
	    }
	} finally {
	    client.disconnect();
	}
    }

    private File getBundle(String bundleID, String bundleVersion)
	    throws IOException {
	Bundle bundle = Platform.getBundle("com.genuitec.piplug.client.test");
	IPath path = new Path("resources/plugins/" + bundleID + "_"
		+ bundleVersion + ".jar");
	URL bundleURL = FileLocator.find(bundle, path, null);
	assertNotNull("url to bundle", bundleURL);
	URL fileURL = FileLocator.toFileURL(bundleURL);
	assertNotNull("file url to bundle", fileURL);
	File sourceFile = new File(fileURL.getFile());
	assertNotNull("file to bundle", sourceFile);
	assertTrue("bundle exists", sourceFile.isFile());
	return sourceFile;
    }

    @AfterClass
    public static void stopDaemon() throws Exception {
	if (daemon != null)
	    daemon.stop();
	if (storageLocation != null)
	    deleteDirectory(storageLocation);
    }

    private static boolean deleteDirectory(File directory) {
	if (directory.exists()) {
	    File[] files = directory.listFiles();
	    if (null != files) {
		for (int i = 0; i < files.length; i++) {
		    if (files[i].isDirectory()) {
			deleteDirectory(files[i]);
		    } else {
			files[i].delete();
		    }
		}
	    }
	}
	return (directory.delete());
    }
}
