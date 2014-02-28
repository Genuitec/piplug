package com.genuitec.piplug.daemon;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import fi.iki.elonen.AbstractFileWebServer;

public class PiPlugDaemon extends AbstractFileWebServer {

    private static final int DAEMON_PORT = 4392;
    private static final boolean DEBUG = "true".equals(System
	    .getProperty("debug.daemon"));

    private DatagramSocket datagramSocket;

    public PiPlugDaemon() {
	super(null, DAEMON_PORT, !DEBUG);
    }

    public static void main(String[] args) throws IOException {
	new PiPlugDaemon().start();
    }

    /**
     * Start the PiPlug daemon including listening for broadcast UDP messages.
     */
    public void start() throws IOException {
	datagramSocket = new DatagramSocket(DAEMON_PORT,
		InetAddress.getByName("0.0.0.0"));
	datagramSocket.setBroadcast(true);
	try {
	    super.start();
	} catch (IOException ioe) {
	    // make sure we clean up if we hit an error during startup
	    datagramSocket.close();
	    throw ioe;
	}
	new DiscoveryThread(datagramSocket).start();
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
	// TODO Auto-generated method stub
	return null;
    }
}
