package com.genuitec.piplug.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.daemon.PiPlugDaemon;

public class AddClockAppToDaemonTest {

    private static String testBundleID = "com.genuitec.piplug.app.clock";
    private static String testBundleVersion = "1.0.0.201403041652";

    /**
     * Test that will:
     * 
     * 1. Discover the location of the server.
     * 
     * 2. Confirm if the clock app is on the server, and if it is not, upload
     * it.
     * 
     * @throws Exception
     */
    @Test
    public void addClockApp() throws Exception {
	PiPlugClient client = new PiPlugClient();
	try {
	    InetSocketAddress discoveredAt = client.discoverServer(30000);
	    assertNotNull("server discovered", discoveredAt);
	    assertEquals("discovered port", PiPlugDaemon.DAEMON_PORT,
		    discoveredAt.getPort());
	    client.connectTo(discoveredAt);

	    BundleDescriptor newDescriptor;
	    newDescriptor = new BundleDescriptor();
	    newDescriptor.setBundleID(testBundleID);
	    newDescriptor.setVersion(Version.parseVersion(testBundleVersion));

	    List<BundleDescriptor> listBundles = client.listBundles();
	    assertNotNull("bundles listing is null", listBundles);
	    for (BundleDescriptor next : listBundles) {
		if (next.matchesIDVersion(newDescriptor))
		    return; // already on server
	    }

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

	    client.uploadBundle(newDescriptor, sourceFile);

	} finally {
	    client.disconnect();
	}
    }
}
