package com.genuitec.piplug.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.InetSocketAddress;

import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.daemon.PiPlugDaemon;

public class PiPlugClientTests {

    private static PiPlugDaemon daemon;
    private static File storageLocation;
    private static InetSocketAddress directAt;

    @BeforeClass
    public static void startDaemon() throws Exception {
	storageLocation = File.createTempFile("piplug", "test");
	storageLocation.delete();
	storageLocation.mkdirs();
	daemon = new PiPlugDaemon(storageLocation, false);
	daemon.start();
	directAt = new InetSocketAddress("127.0.0.1", PiPlugDaemon.DAEMON_PORT);
    }

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

    @Test
    public void listBundlesAtStart() throws CoreException {
	PiPlugClient client = new PiPlugClient();
	try {
	    client.connectTo(directAt);
	    assertNotNull("bundles listing", client.listBundles());
	    assertEquals("no bundles on start", 0, client.listBundles().size());
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
