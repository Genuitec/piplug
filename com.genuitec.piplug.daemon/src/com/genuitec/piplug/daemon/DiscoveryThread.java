package com.genuitec.piplug.daemon;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DiscoveryThread extends Thread {

    private DatagramSocket datagramSocket;
    private boolean debug;

    public DiscoveryThread(DatagramSocket datagramSocket, boolean debug) {
	this.datagramSocket = datagramSocket;
	this.debug = debug;
    }

    public void run() {
	byte buffer[] = new byte[16384];
	DatagramPacket dp;

	try {
	    String expectedReceive = "find-piplug-server";
	    byte[] bytesToSend = "piplug-server".getBytes("UTF-8");

	    while (!datagramSocket.isClosed()) {
		try {
		    dp = new DatagramPacket(buffer, 16384);
		    datagramSocket.receive(dp);
		    String received = new String(buffer, dp.getOffset(),
			    dp.getLength(), "UTF-8");
		    if (expectedReceive.equals(received)) {
			dp = new DatagramPacket(bytesToSend, 0,
				bytesToSend.length, dp.getAddress(),
				dp.getPort());
			if (debug)
			    System.out.println("Responding to discovery from "
				    + dp.getAddress() + ":" + dp.getPort());
			datagramSocket.send(dp);
		    }
		} catch (IOException ex) {
		    if (!datagramSocket.isClosed())
			ex.printStackTrace();
		}
	    }
	} catch (Exception e) {
	    if (debug)
		System.err.println("Unable to receive connections from client");
	    e.printStackTrace();
	}

    }
}
