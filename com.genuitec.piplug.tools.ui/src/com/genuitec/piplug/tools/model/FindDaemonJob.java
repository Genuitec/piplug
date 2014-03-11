package com.genuitec.piplug.tools.model;

import java.net.InetSocketAddress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class FindDaemonJob extends Job {

	private IDaemonStateListener listener;

	public FindDaemonJob(IDaemonStateListener listener) {
		super("Finding PiPlug Daemon");
		this.listener = listener;
		setUser(false);
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		PiPlugCore core = PiPlugCore.getInstance();
		InetSocketAddress serverAddress = core.tryToDiscoverDaemon(false);

		if (serverAddress == null) {
			core.startDaemonLocally();
			serverAddress = core.tryToDiscoverDaemon(true);
		}

		if (serverAddress == null)
		    return Status.OK_STATUS;
		    
		try {
			core.getClient().connectTo(serverAddress);
		} catch (CoreException e) {
			e.printStackTrace();
			return Status.OK_STATUS;
		}
		listener.daemonStateChanged();
		
		try {
			core.getClient().notifyClients();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
