package com.genuitec.piplug.tools.ui.views;

import org.eclipse.ui.IWorkbenchPartSite;

import com.genuitec.piplug.tools.operations.PiPlugDeployOperation;
import com.genuitec.piplug.tools.ui.Activator;

public class PiPlugDeployAction extends PiPlugSelectionListenerAction {

	protected PiPlugDeployAction(IWorkbenchPartSite site) {
		super("Deploy", site);
		setToolTipText("Deploy selected apps");
		setImageDescriptor(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "resources/deploy.gif"));
	}

	public void run() {
		new PiPlugDeployOperation(getSelectedApps()).run();
	}

}
