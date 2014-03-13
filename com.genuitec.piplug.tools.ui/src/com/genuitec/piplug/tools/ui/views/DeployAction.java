package com.genuitec.piplug.tools.ui.views;

import org.eclipse.ui.IWorkbenchPartSite;

import com.genuitec.piplug.tools.operations.DeployOperation;
import com.genuitec.piplug.tools.ui.Activator;

public class DeployAction extends ExtensionSelectionListenerAction {

    protected DeployAction(IWorkbenchPartSite site) {
	super("Deploy", site);
	setToolTipText("Deploy selected apps");
	setImageDescriptor(Activator.imageDescriptorFromPlugin(
		Activator.PLUGIN_ID, "resources/deploy.gif"));
    }

    public void run() {
	new DeployOperation(getSelectedExtensions()).schedule();
    }

}
