package com.genuitec.piplug.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.genuitec.piplug.client.internal.DiscoverDaemonService;

public class PiPlugClient {

    private static final String ID = "com.genuitec.piplug.client";
    private InetSocketAddress connectedTo;

    public InetSocketAddress discoverServer(int timeoutInMillis)
	    throws CoreException {
	return DiscoverDaemonService.discover(timeoutInMillis);
    }

    public void connectTo(InetSocketAddress address) throws CoreException {
	try {
	    Reader raw = new InputStreamReader(urlTo(address, "/connect")
		    .openStream(), "UTF-8");
	    try {
		String response = new BufferedReader(raw).readLine();
		if ("connection-alive".equals(response)) {
		    connectedTo = address;
		} else {
		    IStatus status = new Status(IStatus.ERROR, ID,
			    "Unable to connect to " + address
				    + ": unexpected response [" + response
				    + "]");
		    throw new CoreException(status);
		}
	    } finally {
		raw.close();
	    }
	} catch (IOException ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to connect to " + address, ioe);
	    throw new CoreException(status);
	}
    }

    public void disconnect() throws CoreException {
	// TODO clean up listener thread
	connectedTo = null;
    }

    public List<BundleDescriptor> listBundles() throws CoreException {
	checkConnected();
	// TODO Implement Me
	return new ArrayList<BundleDescriptor>();
    }

    public void downloadBundle(BundleDescriptor descriptor, File targetFile)
	    throws CoreException {
	checkConnected();
	// TODO Implement Me
    }

    public void uploadBundle(BundleDescriptor descriptor, File sourceFile)
	    throws CoreException {
	checkConnected();
	// TODO Implement Me
    }

    public void addListener(IPiPlugClientListener listener) {
	// TODO Implement Me
    }

    public void removeListener(IPiPlugClientListener listener) {
	// TODO Implement Me
    }

    private void checkConnected() throws CoreException {
	if (connectedTo == null) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "You must call connectTo(...) before using this API");
	    throw new CoreException(status);
	}
    }

    private URL urlTo(InetSocketAddress address, String path)
	    throws MalformedURLException {
	return new URL("http", address.getHostName(), address.getPort(), path);
    }
}
