package com.genuitec.piplug.tools.operations;

import java.net.InetSocketAddress;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugExtension;
import com.genuitec.piplug.tools.ui.Activator;

public class RemoveOperation {

	private Set<PiPlugExtension> extensions;

	public RemoveOperation(Set<PiPlugExtension> extensions) {
		this.extensions = extensions;
	}

	public void run() {
		PiPlugClient client = new PiPlugClient();
		InetSocketAddress serverAddress;
		try {
			serverAddress = client.discoverServer(30000);
		} catch (CoreException e) {
			reportError(
					"Could not discover the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return;
		}

		try {
			client.connectTo(serverAddress);
		} catch (CoreException e) {
			reportError(
					"Could not connect to the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return;
		}

		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 42,
				"Deployment of bundles report", null);

		try {
			for (PiPlugExtension extension : extensions) {
				try {
					client.removeBundle(extension.getBundle().getDescriptor());
				} catch (CoreException e) {
					// TODO better error handling
					e.printStackTrace();
				}				
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
		}

	}

	protected void reportError(final String message, final IStatus status) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(null, "Deploy Error", message, status);
			}
		});
	}

}
