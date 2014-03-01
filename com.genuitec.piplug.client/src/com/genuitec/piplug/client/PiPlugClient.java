package com.genuitec.piplug.client;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import com.genuitec.piplug.client.internal.DiscoverDaemonService;

public class PiPlugClient {

    public InetSocketAddress discoverServer(int timeoutInMillis)
	    throws CoreException {
	return DiscoverDaemonService.discover(timeoutInMillis);
    }

    public void connectTo(InetSocketAddress address) throws CoreException {
	// TODO Implement Me
    }

    public void disconnect() {
	// TODO Implement Me
    }

    public List<BundleDescriptor> listBundles() throws CoreException {
	// TODO Implement Me
	return new ArrayList<BundleDescriptor>();
    }

    public void downloadTo(BundleDescriptor descriptor, File targetFile)
	    throws CoreException {
	// TODO Implement Me
    }

    public void uploadFrom(BundleDescriptor descriptor, File sourceFile)
	    throws CoreException {
	// TODO Implement Me
    }

    public void addListener(IPiPlugClientListener listener) {
	// TODO Implement Me
    }

    public void removeListener(IPiPlugClientListener listener) {
	// TODO Implement Me
    }
}
