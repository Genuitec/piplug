package com.genuitec.piplug.daemon;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DiscoveryThread extends Thread {

    private DatagramSocket datagramSocket;

    public DiscoveryThread(DatagramSocket datagramSocket) {
	this.datagramSocket = datagramSocket;
    }

    public void run() {
	byte buffer[] = new byte[16384];
	DatagramPacket dp;

	try {
	    String expectedReceive = "find-piplug-server";
	    byte[] bytesToSend = "piplug-server".getBytes("UTF-8");

	    while (true) {
		try {
		    dp = new DatagramPacket(buffer, 16384);
		    datagramSocket.receive(dp);
		    String received = new String(buffer, dp.getOffset(),
			    dp.getLength(), "UTF-8");
		    if (expectedReceive.equals(received)) {
			dp = new DatagramPacket(bytesToSend, 0,
				bytesToSend.length, dp.getAddress(),
				dp.getPort());
			System.out.println("Responding to discovery from "
				+ dp.getAddress() + ":" + dp.getPort());
			datagramSocket.send(dp);
		    }
		} catch (IOException ex) {
		    System.out.println("Error listening to clients");
		}
	    }
	} catch (Exception e) {
	    System.err.println("Unable to receive connections from client");
	    e.printStackTrace();
	}

    }
}
