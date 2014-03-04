package com.genuitec.piplug.client.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class DiscoverDaemonService extends Thread {

    private static final int DAEMON_PORT = 4392;
    private static final String PLUGIN_ID = "com.genuitec.piplug.client";

    protected DatagramSocket socket;
    protected boolean received;
    protected byte[] bytesToSend;

    private DiscoverDaemonService(DatagramSocket socket, byte[] bytesToSend) {
	this.socket = socket;
	this.received = false;
	this.bytesToSend = bytesToSend;
    }

    public void run() {
	while (!received) {
	    // re-send every quarter second
	    try {
		Thread.sleep(250);
	    } catch (Exception ignore) {
	    }

	    // broadcast on all the networks connected
	    try {
		for (NetworkInterface nic : Collections.list(NetworkInterface
			.getNetworkInterfaces())) {
		    for (InterfaceAddress ip : nic.getInterfaceAddresses()) {
			if (ip == null || ip.getBroadcast() == null) {
			    continue;
			}
			sendDiscovery(ip);
		    }
		}
	    } catch (Exception ignore) {
		ignore.printStackTrace();
		// best effort
	    }
	}
    }

    private void sendDiscovery(InterfaceAddress ip) {
	InetAddress broadcast = ip.getBroadcast();

	// see if we are getting hit by:
	// http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7158636
	if (ip.getAddress().getAddress()[0] == broadcast.getAddress()[0]) {

	    // broadcast passes the sniff test for the bug, just use what is
	    // provided by the JVM
	    sendDiscoveryTo(broadcast);

	} else {

	    // hacks to see if we can discover the correct broadcast address
	    // thanks to windows WLAN adapters under Java not always
	    // returning the right address for us

	    // see if we can fix the address by network prefix
	    if (ip.getNetworkPrefixLength() >= 8) {

		byte[] addr = ip.getAddress().getAddress();
		StringBuffer bitMask = new StringBuffer();
		int i = 0;
		while (i < ip.getNetworkPrefixLength()) {
		    bitMask.append('0');
		    i++;
		}
		while (i < 32) {
		    bitMask.append('1');
		    i++;
		}
		for (i = 0; i < 4; i++) {
		    String maskStr = bitMask.substring(i * 8, (i + 1) * 8);
		    byte mask = (byte) (Integer.parseInt(maskStr, 2) & 0xff);
		    addr[i] = (byte) ((addr[i] | mask) & 0xff);
		}
		try {
		    sendDiscoveryTo(InetAddress.getByAddress(addr));
		} catch (UnknownHostException ignored) {
		}

	    }

	    // change the last segment to .255
	    byte[] addr = ip.getAddress().getAddress();
	    addr[3] = (byte) 255;
	    try {
		sendDiscoveryTo(InetAddress.getByAddress(addr));
	    } catch (UnknownHostException ignored) {
	    }

	    // change the last two segments to .255.255
	    addr[2] = (byte) 255;
	    try {
		sendDiscoveryTo(InetAddress.getByAddress(addr));
	    } catch (UnknownHostException ignored) {
	    }
	}
    }

    private void sendDiscoveryTo(InetAddress broadcast) {
	try {
	    DatagramPacket dp = new DatagramPacket(bytesToSend, 0,
		    bytesToSend.length, broadcast, DAEMON_PORT);
	    socket.send(dp);
	} catch (Exception e) {
	    // best effort
	}
    }

    public static InetSocketAddress discover(int timeoutInMillis)
	    throws CoreException {
	try {
	    byte[] bytesToSend = "find-piplug-server".getBytes("UTF-8");
	    String expectedToReceive = "piplug-server";

	    DatagramSocket client = new DatagramSocket();
	    client.setBroadcast(true);

	    DiscoverDaemonService thread = new DiscoverDaemonService(client,
		    bytesToSend);
	    thread.start();
	    try {
		// wait for a server to respond
		if (timeoutInMillis > 0)
		    client.setSoTimeout(timeoutInMillis);
		byte[] buffer = new byte[16384];
		DatagramPacket dp = new DatagramPacket(buffer, 0,
			bytesToSend.length);
		try {
		    client.receive(dp);
		} catch (SocketTimeoutException ste) {
		    throw new CoreException(new Status(IStatus.ERROR,
			    PLUGIN_ID,
			    "Unable to discover server in remaining time", ste));
		}
		String received = new String(buffer, dp.getOffset(),
			dp.getLength(), "UTF-8");
		if (expectedToReceive.equals(received))
		    return new InetSocketAddress(dp.getAddress(), dp.getPort());
		throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
			"Received unexpected data in response"));
	    } finally {
		thread.received = true;
	    }
	} catch (IOException ioe) {
	    throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
		    "Error encountered attempting to discover server", ioe));
	}
    }
}
