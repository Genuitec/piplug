package com.genuitec.piplug.tools.ui.views;

import org.eclipse.ui.IWorkbenchPartSite;

import com.genuitec.piplug.tools.operations.UndeployOperation;

public class UndeployAction extends ExtensionSelectionListenerAction {

	protected UndeployAction(IWorkbenchPartSite site) {
		super("Undeploy", site);
		setToolTipText("Undeploy the selected apps from the daemon");
//		setImageDescriptor(Activator.imageDescriptorFromPlugin(
//				Activator.PLUGIN_ID, "resources/deploy.gif"));
	}

	public void run() {
		new UndeployOperation(getSelectedExtensions()).schedule();
	}

}
