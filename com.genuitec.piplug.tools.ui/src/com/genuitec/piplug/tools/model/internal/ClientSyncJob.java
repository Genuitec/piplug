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
import com.genuitec.piplug.tools.ui.Activator;

public class ClientSyncJob extends Job {

	private static final int DISCOVER_TIMEOUT = 30000;
	private PiPlugClient client;

	public ClientSyncJob(PiPlugClient client) {
		super("PiPlug Client Sync");
		this.client = client;
		setUser(false);
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Synchronizing PiPlug models",
				DISCOVER_TIMEOUT + 10000);
		try {
			InetSocketAddress serverAddress = tryToDiscoverDaemon(false);

			if (serverAddress == null) {
				PiPlugCore.getInstance().startDaemonLocally();
				serverAddress = tryToDiscoverDaemon(true);
			}

			if (serverAddress == null)
				return Status.OK_STATUS;

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

	private InetSocketAddress tryToDiscoverDaemon(boolean logError) {
		try {
			return client.discoverServer(DISCOVER_TIMEOUT);
		} catch (CoreException e) {
			if (logError)
				Activator
						.getDefault()
						.getLog()
						.log(new Status(
								IStatus.ERROR,
								Activator.PLUGIN_ID,
								"Could not discover running PiPlug daemon process",
								e));

			return null;
		}
	}

}
