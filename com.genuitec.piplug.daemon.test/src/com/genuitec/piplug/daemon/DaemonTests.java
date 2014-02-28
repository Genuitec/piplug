package com.genuitec.piplug.daemon;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class DaemonTests {

    private static final int DAEMON_PORT = 4392;

	@Test
	public void discovery() throws IOException {

	    byte[] bytesToSend = "find-piplug-server".getBytes("UTF-8");
	    String expectedToReceive = "piplug-server";

		DatagramSocket client = new DatagramSocket();
		client.setBroadcast(true);

		// broadcast on all the networks connected
		for(NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())){
			for(InterfaceAddress ip : nic.getInterfaceAddresses()){
				if(ip.getBroadcast() == null){
					continue;
				}
				System.out.println("Now broadcasting to " + ip.getBroadcast().getHostAddress());
				try{
					DatagramPacket dp = new DatagramPacket(bytesToSend, 0, bytesToSend.length, ip.getBroadcast(), DAEMON_PORT);
					client.send(dp);
				}catch(Exception e){
					System.out.println("Sending secret code error " + e);
				}													
			}
		}
		
		// wait for a server to respond
		client.setSoTimeout(5000); // only wait 5 seconds for a server (either running or not)
		byte[] buffer = new byte[16384];
		DatagramPacket dp = new DatagramPacket(buffer, 0, bytesToSend.length);
		client.receive(dp);
	    String received = new String(buffer, dp.getOffset(),
			    dp.getLength(), "UTF-8");
	    Assert.assertEquals(expectedToReceive, received);
	    
	    System.out.println("Discovered server at "+dp.getAddress()+":"+dp.getPort());
	}
}
