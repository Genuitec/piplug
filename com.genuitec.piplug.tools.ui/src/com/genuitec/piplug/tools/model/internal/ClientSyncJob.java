package com.genuitec.piplug.tools.model.internal;

import java.net.InetSocketAddress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.genuitec.piplug.client.BundleDescriptors;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugCore;

public class ClientSyncJob extends Job {

	private static final int DISCOVER_TIMEOUT = 30000;
	private PiPlugClient client;

	public ClientSyncJob(PiPlugClient client) {
		super("PiPlug Client Sync");
		this.client = client;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Synchronizing PiPlug models", DISCOVER_TIMEOUT + 10000);
		try {
			InetSocketAddress serverAddress;
			try {
				serverAddress = client.discoverServer(DISCOVER_TIMEOUT);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Status.OK_STATUS;
			}
			monitor.worked(DISCOVER_TIMEOUT);
			try {
				client.connectTo(serverAddress);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Status.OK_STATUS;
			}
			monitor.worked(1000);
			
			try {
				BundleDescriptors bundles = client.getBundlesFromCache();
				PiPlugCore.getInstance().setRemoteBundleDescriptors(bundles);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Status.OK_STATUS;
			}
			
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

}
