package com.genuitec.piplug.daemon;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.genuitec.piplug.common.BundleDescriptor;
import com.genuitec.piplug.common.BundleDescriptors;

import fi.iki.elonen.AbstractFileWebServer;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class PiPlugDaemon extends AbstractFileWebServer {

    private static final int EVENT_NOTIFICATION_DELAY = 15000;

    public static final int DAEMON_PORT = 4392;

    private DatagramSocket datagramSocket;
    private BundleDescriptors bundleDescriptors = new BundleDescriptors();
    private JAXBContext jaxb;
    private File storageLocation;
    private EventBatchingSemaphore eventSemaphore;

    public PiPlugDaemon(File storageLocation, boolean debug) {
	super(null, DAEMON_PORT, !debug); // null = bound to all interfaces
	this.storageLocation = storageLocation;
	try {
	    jaxb = JAXBContext.newInstance(BundleDescriptors.class);
	} catch (Exception e) {
	    throw new IllegalStateException(
		    "Unable to prepare JAXB context for serialization");
	}
	bundleDescriptors.setDescriptors(new ArrayList<BundleDescriptor>());
	bundleDescriptors.setSyncTime(System.currentTimeMillis());
	loadBundlesList();
	if (debug)
	    System.out.println("Initial daemon sync time: "
		    + bundleDescriptors.getSyncTime());
	eventSemaphore = new EventBatchingSemaphore(bundleDescriptors, debug);
    }

    public static void main(String[] args) throws IOException {
	if (args.length == 0) {
	    System.err
		    .println("usage: java -jar piplug-daemon.jar storage-location");
	    System.exit(-1);
	    return;
	}

	File storage = new File(args[0]);
	boolean debug = "true".equals(System.getProperty("daemon.debug"));
	System.out.println("PiPlug daemon starting up on port " + DAEMON_PORT);
	new PiPlugDaemon(storage, debug).start();
    }

    /**
     * Start the PiPlug daemon including listening for broadcast UDP messages.
     */
    public void start() throws IOException {
	InetAddress addr = InetAddress.getByName("0.0.0.0");
	datagramSocket = new DatagramSocket(DAEMON_PORT, addr);
	datagramSocket.setBroadcast(true);
	try {
	    super.start();
	} catch (IOException ioe) {
	    // make sure we clean up if we hit an error during startup
	    datagramSocket.close();
	    throw ioe;
	}
	new DiscoveryThread(datagramSocket, !quiet).start();
    }

    /**
     * Stop the PiPlug daemon.
     */
    public void stop() {
	super.stop();
	try {
	    if (null != datagramSocket)
		datagramSocket.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    protected Response handle(IHTTPSession session) {
	try {
	    String uri = session.getUri();
	    if (uri.equals("/connect"))
		return new Response(Status.OK, "text/ascii", "connection-alive");
	    if (uri.equals("/list-bundles"))
		return listBundles();
	    if (uri.equals("/remove-bundle"))
		return removeBundle(session);
	    if (uri.equals("/put-bundle"))
		return putBundle(session);
	    if (uri.equals("/get-bundle"))
		return getBundle(session);
	    if (uri.equals("/wait-for-changes"))
		return getChangedBundles(session);

	    // File Not Found
	    return null;
	} catch (Exception e) {
	    e.printStackTrace();
	    return new Response(Status.INTERNAL_ERROR, "text/ascii",
		    "Could not process or handle request");
	}
    }

    private Response getChangedBundles(IHTTPSession session)
	    throws JAXBException {
	long clientSyncTime = getClientSyncTime(session);
	eventSemaphore.canReturnEvents(clientSyncTime);

	Marshaller marshaller = jaxb.createMarshaller();
	StringWriter sw = new StringWriter();
	BundleDescriptors clone;
	synchronized (bundleDescriptors) {
	    clone = (BundleDescriptors) bundleDescriptors.clone();
	}
	marshaller.marshal(clone, sw);
	return new Response(Status.OK, "text/xml", sw.toString());
    }

    private long getClientSyncTime(IHTTPSession session) {
	String time = session.getHeaders().get("client-sync-time");
	if (time == null)
	    return 0;
	try {
	    return Long.parseLong(time);
	} catch (Exception e) {
	    return 0;
	}
    }

    private Response getBundle(IHTTPSession session) {
	BundleDescriptor toMatch = new BundleDescriptor();
	toMatch.setBundleID(session.getHeaders().get("bundle-id"));
	toMatch.setVersion(session.getHeaders().get("bundle-version"));
	if (toMatch.getBundleID() == null || toMatch.getVersion() == null) {
	    return new Response(Status.BAD_REQUEST, "text/ascii",
		    "Missing bundle headers");
	}

	BundleDescriptor match = null;
	synchronized (bundleDescriptors) {
	    match = bundleDescriptors.matchByIDVersion(toMatch);
	}
	if (match != null) {
	    File toReturn = getPathTo(match);
	    if (!toReturn.isFile())
		return new Response(Status.NOT_FOUND, "text/ascii",
			"bundle-file-not-found");
	    InputStream in;
	    try {
		in = new FileInputStream(toReturn);
	    } catch (IOException ioe) {
		return new Response(Status.NOT_FOUND, "text/ascii",
			"bundle-file-not-openable");
	    }
	    in = new BufferedInputStream(in, 1024 * 1024);
	    return new Response(Status.OK, "application/octet-stream", in);
	}
	return new Response(Status.NOT_FOUND, "text/ascii", "bundle-not-found");
    }

    private Response putBundle(IHTTPSession session) {
	BundleDescriptor newDescriptor = new BundleDescriptor();
	newDescriptor.setBundleID(session.getHeaders().get("bundle-id"));
	newDescriptor.setVersion(session.getHeaders().get("bundle-version"));
	if (newDescriptor.getBundleID() == null
		|| newDescriptor.getVersion() == null) {
	    return new Response(Status.BAD_REQUEST, "text/ascii",
		    "Missing bundle headers");
	}

	Set<BundleDescriptor> existingDescriptors = null;
	synchronized (bundleDescriptors) {
	    existingDescriptors = bundleDescriptors.matchesByID(newDescriptor);
	}
	newDescriptor.setLastUpdatedOn(new Date());
	if (!existingDescriptors.isEmpty())
	    newDescriptor.setFirstAdded(existingDescriptors.iterator().next()
		    .getFirstAdded());
	else
	    newDescriptor.setFirstAdded(newDescriptor.getLastUpdatedOn());

	InputStream in = session.getInputStream();
	File targetFile = getPathTo(newDescriptor);
	File newFile = new File(targetFile.getParentFile(),
		targetFile.getName() + ".new");
	storageLocation.mkdirs();
	int contentLength = Integer.parseInt(session.getHeaders().get(
		"content-length"));
	boolean cleanup = true;
	try {
	    OutputStream out = new FileOutputStream(newFile);
	    try {
		byte buffer[] = new byte[1024 * 1024];
		int numread;
		int remaining = contentLength;
		while ((remaining > 0) && (0 <= (numread = in.read(buffer)))) {
		    out.write(buffer, 0, numread);
		    remaining -= numread;
		}
		cleanup = false;
	    } finally {
		out.close();
		if (cleanup)
		    newFile.delete();
	    }
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	    return new Response(Status.INTERNAL_ERROR, "text/ascii",
		    "Unable to receive file");
	}
	if (newFile.length() != contentLength) {
	    newFile.delete();
	    return new Response(Status.BAD_REQUEST, "text/ascii",
		    "Wrong number of bytes received");
	}
	targetFile.delete();
	newFile.renameTo(targetFile);

	synchronized (bundleDescriptors) {
	    System.out.println("Before: " + bundleDescriptors.getDescriptors());
	    for (BundleDescriptor bundleDescriptor : existingDescriptors) {
		if (!bundleDescriptors.remove(bundleDescriptor))
		    System.out.println("Failed to remove " + bundleDescriptor);
	    }
	    bundleDescriptors.add(newDescriptor);
	    saveBundlesList();
	    System.out.println("After: " + bundleDescriptors.getDescriptors());
	}

	for (BundleDescriptor existingDescriptor : existingDescriptors) {
	    if (!existingDescriptor.getVersion().equals(
		    newDescriptor.getVersion())) {
		getPathTo(existingDescriptor).delete();
	    }
	}

	return new Response(Status.OK, "text/ascii", "uploaded-bundle");
    }

    private Response removeBundle(IHTTPSession session) {
	BundleDescriptor toMatch = new BundleDescriptor();
	toMatch.setBundleID(session.getHeaders().get("bundle-id"));
	toMatch.setVersion(session.getHeaders().get("bundle-version"));
	if (toMatch.getBundleID() == null || toMatch.getVersion() == null) {
	    return new Response(Status.BAD_REQUEST, "text/ascii",
		    "Missing bundle headers");
	}

	boolean match = false;
	synchronized (bundleDescriptors) {
	    BundleDescriptor matched = bundleDescriptors
		    .matchByIDVersion(toMatch);
	    if (matched != null) {
		bundleDescriptors.remove(matched);
		match = true;
	    }
	    saveBundlesList();
	}
	if (match) {
	    File toDelete = getPathTo(toMatch);
	    if (!toDelete.delete())
		toDelete.deleteOnExit();
	    return new Response(Status.OK, "text/ascii", "removed-bundle");
	}

	return new Response(Status.NOT_FOUND, "text/ascii", "bundle-not-found");
    }

    private void loadBundlesList() {
	File bundlesFile = getBundlesListFile();
	if (bundlesFile.exists()) {
	    try {
		Unmarshaller unmarshaller = jaxb.createUnmarshaller();
		BundleDescriptors persistDescriptors = (BundleDescriptors) unmarshaller
			.unmarshal(bundlesFile);
		bundleDescriptors = persistDescriptors;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	if (!quiet)
	    System.out.println("Loaded Bundles: " + bundleDescriptors);
    }

    private void saveBundlesList() {
	try {
	    Marshaller marshaller = jaxb.createMarshaller();
	    marshaller.marshal(bundleDescriptors, getBundlesListFile());

	    // Notify listeners of new bundles after a delay
	    eventSemaphore.cancel();
	    eventSemaphore.schedule(EVENT_NOTIFICATION_DELAY);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private File getBundlesListFile() {
	return new File(storageLocation, "bundles.xml");
    }

    private File getPathTo(BundleDescriptor desc) {
	return new File(storageLocation, desc.getBundleID() + "_"
		+ desc.getVersion() + ".jar");
    }

    private Response listBundles() throws JAXBException {
	Marshaller marshaller = jaxb.createMarshaller();
	BundleDescriptors response;
	synchronized (bundleDescriptors) {
	    response = (BundleDescriptors) bundleDescriptors.clone();
	}
	StringWriter sw = new StringWriter();
	marshaller.marshal(response, sw);
	return new Response(Status.OK, "text/xml", sw.toString());
    }
}
