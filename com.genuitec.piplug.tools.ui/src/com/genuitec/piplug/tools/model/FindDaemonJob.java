package com.genuitec.piplug.tools.model;

import java.net.InetSocketAddress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.ui.Activator;

public class FindDaemonJob extends Job {

    private IDaemonStateListener listener;

    public FindDaemonJob(IDaemonStateListener listener) {
	super("Discovering PiPlug Daemon");
	this.listener = listener;
	setUser(false);
	setSystem(false);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
	monitor.beginTask("Discovering PiPlug Daemon",
		PiPlugClient.CONNECT_TIMEOUT);
	IntervalProgressJob intervalProgressJob = new IntervalProgressJob(
		monitor, PiPlugClient.CONNECT_TIMEOUT + 5000);
	intervalProgressJob.schedule();
	try {
	    PiPlugCore core = PiPlugCore.getInstance();

	    InetSocketAddress serverAddress;
	    boolean connected = false;

	    // first try 127.0.0.1 as we avoid delays finding daemon, and IPs
	    // changing in DHCP
	    try {
		InetSocketAddress localAddress = new InetSocketAddress(
			"127.0.0.1", 4392);
		core.getClient().connectTo(localAddress);
		serverAddress = localAddress;
		connected = true;
		Activator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
				"Discovered PiPlug daemon running on localhost"));
		;
	    } catch (CoreException e) {
		// expected to fail when the daemon isn't already running
		serverAddress = null;
	    }

	    if (!connected) {
		serverAddress = core.tryToDiscoverDaemon(false);
		Activator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
				"Discovered PiPlug daemon running at "
					+ serverAddress));
	    }

	    if (serverAddress == null) {
		try {
		    core.startDaemonLocally();
		    // give the daemon some time to start up
		    Thread.sleep(5000);
		    InetSocketAddress localAddress = new InetSocketAddress(
			    "127.0.0.1", 4392);
		    core.getClient().connectTo(localAddress);
		    Activator
			    .getDefault()
			    .getLog()
			    .log(new Status(
				    IStatus.INFO,
				    Activator.PLUGIN_ID,
				    "Connected to PiPlug daemon initiated by PiPlug Deploy view (running within Eclipse instance)"));
		    serverAddress = localAddress;
		    connected = true;
		} catch (Exception e) {
		    Activator
			    .getDefault()
			    .getLog()
			    .log(new Status(
				    IStatus.INFO,
				    Activator.PLUGIN_ID,
				    "Unable to start or connect to daemon running within Eclipse instance; will retry in 15 seconds",
				    e));
		    schedule(15000);
		    return Status.OK_STATUS;
		}
	    }

	    if (!connected) {
		try {
		    core.getClient().connectTo(serverAddress);
		} catch (CoreException e) {
		    Activator.getDefault().getLog().log(e.getStatus());
		    schedule(15000);
		    return Status.OK_STATUS;
		}
	    }
	    listener.daemonStateChanged();

	    try {
		PiPlugCore.getInstance().newBundleList(
			core.getClient().getBundlesFromCache());
	    } catch (CoreException e) {
		Activator.getDefault().getLog().log(e.getStatus());
	    }
	    return Status.OK_STATUS;
	} finally {
	    intervalProgressJob.cancel();
	    monitor.done();
	}
    }

}
