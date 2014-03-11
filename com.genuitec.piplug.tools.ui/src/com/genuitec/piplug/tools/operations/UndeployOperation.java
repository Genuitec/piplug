package com.genuitec.piplug.tools.operations;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugCore;
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

		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 42,
				"Undeploy result", null);
		monitor.beginTask("Undeploying applications", extensions.size());
		try {
			PiPlugClient client = PiPlugCore.getInstance().getClient();
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
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}
