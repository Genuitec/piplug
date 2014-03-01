package com.genuitec.piplug.daemon;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import fi.iki.elonen.AbstractFileWebServer;

public class PiPlugDaemon extends AbstractFileWebServer {

    public static final int DAEMON_PORT = 4392;

    private DatagramSocket datagramSocket;

    public PiPlugDaemon(File storageLocation, boolean debug) {
	super(null, DAEMON_PORT, !debug); // null = bound to all interfaces
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
	    datagramSocket.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    protected Response handle(IHTTPSession session) {
	if (session.getUri().startsWith("/plugin/")) {

	}
	// TODO Auto-generated method stub
	return null;
    }
}
