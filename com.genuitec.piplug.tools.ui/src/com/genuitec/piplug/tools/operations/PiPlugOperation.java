package com.genuitec.piplug.tools.operations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

import com.genuitec.piplug.tools.model.PiPlugCore;

public abstract class PiPlugOperation extends Job {

    public PiPlugOperation(String name) {
	super(name);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
	try {
	    PiPlugCore.getInstance().waitForDaemon();
	} catch (CoreException e) {
	    reportError(
		    "Deploy Error",
		    "Could not connect to a PiPlug daemon.\n\nHave you tried starting one locally?",
		    e.getStatus());
	    return Status.OK_STATUS;
	}

	return doRun(monitor);
    }

    protected abstract IStatus doRun(IProgressMonitor monitor);

    protected void reportError(final String dialogTitle, final String message,
	    final IStatus status) {
	PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
	    public void run() {
		ErrorDialog.openError(null, dialogTitle, message, status);
	    }
	});
    }
}
