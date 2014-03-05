package com.genuitec.piplug.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

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
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.daemon.PiPlugDaemon;

public class PiPlugClientTests {

    private static PiPlugDaemon daemon;
    private static File storageLocation;
    private static InetSocketAddress directAt;
    private static String testBundleID = "com.genuitec.piplug.app.clock";
    private static String testBundleVersion = "1.0.0.201403041652";

    @BeforeClass
    public static void startDaemon() throws Exception {
	storageLocation = File.createTempFile("piplug", "test");
	storageLocation.delete();
	storageLocation.mkdirs();
	daemon = new PiPlugDaemon(storageLocation, false);
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

	    Bundle bundle = Platform
		    .getBundle("com.genuitec.piplug.client.test");
	    IPath path = new Path("resources/plugins/" + testBundleID + "_"
		    + testBundleVersion + ".jar");
	    URL bundleURL = FileLocator.find(bundle, path, null);
	    assertNotNull("url to bundle", bundleURL);
	    URL fileURL = FileLocator.toFileURL(bundleURL);
	    assertNotNull("file url to bundle", fileURL);
	    File sourceFile = new File(fileURL.getFile());
	    assertNotNull("file to bundle", sourceFile);
	    assertTrue("bundle exists", sourceFile.isFile());

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
