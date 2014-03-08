package com.genuitec.piplug.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.BundleDescriptors;
import com.genuitec.piplug.client.IPiPlugClientListener;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.daemon.PiPlugDaemon;

public class PiPlugClientTests {

    private static final int LISTENER_TIMEOUT = 10000;

    public class TestListener implements IPiPlugClientListener {

	private BundleDescriptors descriptors = null;
	private boolean duplicateEvents = false;
	private PiPlugClient client;

	public TestListener(PiPlugClient client) {
	    this.client = client;
	}

	public BundleDescriptors waitForDescriptors(long timeout)
		throws InterruptedException {
	    if (duplicateEvents)
		fail("Received duplicate events");
	    if (descriptors != null)
		return descriptors;
	    synchronized (this) {
		wait(timeout);
		return descriptors;
	    }
	}

	public void reset() {
	    duplicateEvents = false;
	    descriptors = null;
	    try {
		System.out.println("Client sync time: "
			+ client.getBundlesFromCache().getSyncTime());
	    } catch (CoreException e) {
		e.printStackTrace();
	    }
	}

	@Override
	public void newBundleList(BundleDescriptors descriptors) {
	    if (this.descriptors != null)
		duplicateEvents = true;
	    synchronized (this) {
		this.descriptors = descriptors;
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
	    TestListener testListener = new TestListener(client);
	    client.addListener(testListener);

	    File sourceFile = getBundle(testBundleID, testBundleVersion);

	    BundleDescriptor descriptor = null;
	    BundleDescriptor newDescriptor = null;

	    descriptor = new BundleDescriptor();
	    descriptor.setBundleID(testBundleID);
	    descriptor.setVersion(testBundleVersion);

	    try {
		testListener.reset();
		client.uploadBundle(descriptor, sourceFile);
		BundleDescriptors descriptors = testListener
			.waitForDescriptors(LISTENER_TIMEOUT);

		assertNotNull("Timout during wait for addition", descriptors);
		Set<BundleDescriptor> additions = descriptors
			.matchesByIDVersion(descriptor);
		assertTrue("Did not get an added bundle", !additions.isEmpty());
		System.out.println("Got addition");

		newDescriptor = new BundleDescriptor();
		newDescriptor.setBundleID(testBundleID);
		newDescriptor.setVersion(testBundleVersion + "1");

		testListener.reset();
		client.uploadBundle(newDescriptor, sourceFile);
		descriptors = testListener.waitForDescriptors(LISTENER_TIMEOUT);

		Set<BundleDescriptor> removals = descriptors
			.matchesByIDVersion(descriptor);
		assertTrue("Did not remove the old bundle", removals.isEmpty());
		additions = descriptors.matchesByIDVersion(newDescriptor);
		assertTrue("Did not get a changed bundle", !additions.isEmpty());
		System.out.println("Got changed");
		descriptor = null;

		testListener.reset();
		client.removeBundle(newDescriptor);
		descriptors = testListener.waitForDescriptors(LISTENER_TIMEOUT);

		removals = descriptors.matchesByIDVersion(newDescriptor);
		assertTrue("Did not remove the bundle", removals.isEmpty());
		System.out.println("Got removed");

		// Null this out so we don't try to remove it again
		newDescriptor = null;

	    } finally {
		// Do our best to clean up the daemon
		try {
		    if (descriptor != null)
			client.removeBundle(descriptor);
		    if (newDescriptor != null)
			client.removeBundle(newDescriptor);
		} catch (Exception e) {
		    // print out but let real exception through
		    e.printStackTrace();
		}
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
	    TestListener testListener = new TestListener(client);
	    client.addListener(testListener);

	    File sourceFile1 = getBundle(testBundleID, testBundleVersion);
	    File sourceFile2 = getBundle(testBundle2ID, testBundle2Version);

	    BundleDescriptor descriptor1 = null;
	    BundleDescriptor descriptor2 = null;

	    descriptor1 = new BundleDescriptor();
	    descriptor1.setBundleID(testBundleID);
	    descriptor1.setVersion(testBundleVersion);

	    descriptor2 = new BundleDescriptor();
	    descriptor2.setBundleID(testBundle2ID);
	    descriptor2.setVersion(testBundle2Version);

	    try {
		testListener.reset();
		client.uploadBundle(descriptor1, sourceFile1);
		client.uploadBundle(descriptor2, sourceFile2);

		BundleDescriptors batchedDescriptors = testListener
			.waitForDescriptors(LISTENER_TIMEOUT);
		Set<BundleDescriptor> additions = batchedDescriptors
			.matchesByIDVersion(descriptor1, descriptor2);
		assertEquals(2, additions.size());

		testListener.reset();
		client.removeBundle(descriptor1);
		client.removeBundle(descriptor2);

		batchedDescriptors = testListener
			.waitForDescriptors(LISTENER_TIMEOUT);
		Set<BundleDescriptor> removals = batchedDescriptors
			.matchesByIDVersion(descriptor1, descriptor2);
		assertEquals(0, removals.size());
		descriptor1 = null;
		descriptor2 = null;
	    } finally {
		try {
		    // Do our best to clean up the daemon
		    if (descriptor1 != null)
			client.removeBundle(descriptor1);
		    if (descriptor2 != null)
			client.removeBundle(descriptor2);
		} catch (Exception e) {
		    // allow real exception through
		    e.printStackTrace();
		}
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
