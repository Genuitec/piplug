package com.genuitec.piplug.tools.operations;

import java.net.InetSocketAddress;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugExtension;
import com.genuitec.piplug.tools.ui.Activator;

public class UndeployOperation extends PiPlugOperation {

	private Set<PiPlugExtension> extensions;

	public UndeployOperation(Set<PiPlugExtension> extensions) {
		super("Undeploying applications");
		this.extensions = extensions;
	}

	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		PiPlugClient client = new PiPlugClient();
		InetSocketAddress serverAddress;
		try {
			serverAddress = client.discoverServer(30000);
		} catch (CoreException e) {
			reportError(
					"Undeploy Error",
					"Could not discover the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return Status.OK_STATUS;
		}

		try {
			client.connectTo(serverAddress);
		} catch (CoreException e) {
			reportError(
					"Undeploy Error",
					"Could not connect to the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return Status.OK_STATUS;
		}

		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 42,
				"Undeploy result", null);
		monitor.beginTask("Undeploying applications", extensions.size());
		try {
			for (PiPlugExtension extension : extensions) {
				try {
					client.removeBundle(extension.getBundle().getDescriptor());
				} catch (CoreException e) {
					// TODO better error handling
					e.printStackTrace();
				}
				monitor.worked(1);
			}
			try {
				client.notifyClients();
			} catch (CoreException e) {
				// TODO better error handling
				e.printStackTrace();
			}
		} finally {

			try {
				client.disconnect();
			} catch (CoreException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
								"Could not disconnect from the PiPlug Daemon.",
								e));
			}
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}
