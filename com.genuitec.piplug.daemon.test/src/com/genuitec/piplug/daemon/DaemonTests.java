package com.genuitec.piplug.daemon;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class DaemonTests {

    private static final int DAEMON_PORT = 4392;

    private static final class SendDiscoveryThread extends Thread {

	protected DatagramSocket socket;
	protected boolean received;
	protected byte[] bytesToSend;

	public SendDiscoveryThread(DatagramSocket socket, byte[] bytesToSend) {
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
		    for (NetworkInterface nic : Collections
			    .list(NetworkInterface.getNetworkInterfaces())) {
			for (InterfaceAddress ip : nic.getInterfaceAddresses()) {

			    if (ip.getBroadcast() == null) {
				continue;
			    }
			    sendDiscovery(ip);
			}
		    }
		} catch (Exception ignore) {
		    // best effort
		}
	    }
	}

	private void sendDiscovery(InterfaceAddress ip) {
	    InetAddress broadcast = ip.getBroadcast();
	    sendDiscoveryTo(broadcast);

	    // see if we are getting hit by:
	    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7158636
	    if (ip.getAddress().getAddress()[0] != ip.getBroadcast()
		    .getAddress()[0]) {

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
		System.out.println("Broadcasting discovery request to "
			+ broadcast);
		DatagramPacket dp = new DatagramPacket(bytesToSend, 0,
			bytesToSend.length, broadcast, DAEMON_PORT);
		socket.send(dp);
	    } catch (Exception e) {
		// best effort
	    }
	}
    }

    @Test
    public void discovery() throws IOException {

	byte[] bytesToSend = "find-piplug-server".getBytes("UTF-8");
	String expectedToReceive = "piplug-server";

	DatagramSocket client = new DatagramSocket();
	client.setBroadcast(true);

	System.out.println("Looking for PiPlug plug-in daemon...");

	SendDiscoveryThread thread = new SendDiscoveryThread(client,
		bytesToSend);
	thread.start();
	try {
	    // wait for a server to respond
	    client.setSoTimeout(30000); // wait 30 seconds for a server
	    byte[] buffer = new byte[16384];
	    DatagramPacket dp = new DatagramPacket(buffer, 0,
		    bytesToSend.length);
	    client.receive(dp);
	    String received = new String(buffer, dp.getOffset(),
		    dp.getLength(), "UTF-8");
	    Assert.assertEquals(expectedToReceive, received);

	    System.out.println("Discovered server at " + dp.getAddress() + ":"
		    + dp.getPort());
	} finally {
	    thread.received = true;
	}
    }
}
