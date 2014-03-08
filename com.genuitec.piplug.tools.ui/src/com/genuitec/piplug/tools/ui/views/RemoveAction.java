package com.genuitec.piplug.tools.ui.views;

import org.eclipse.ui.IWorkbenchPartSite;

import com.genuitec.piplug.tools.operations.RemoveOperation;

public class RemoveAction extends ExtensionSelectionListenerAction {

	protected RemoveAction(IWorkbenchPartSite site) {
		super("Undeploy", site);
		setToolTipText("Remove the selected apps from the daemon");
//		setImageDescriptor(Activator.imageDescriptorFromPlugin(
//				Activator.PLUGIN_ID, "resources/deploy.gif"));
	}

	public void run() {
		new RemoveOperation(getSelectedExtensions()).run();
	}

}
