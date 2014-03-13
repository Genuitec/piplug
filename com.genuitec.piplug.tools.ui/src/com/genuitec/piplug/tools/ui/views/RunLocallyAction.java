package com.genuitec.piplug.tools.ui.views;

import org.eclipse.ui.IWorkbenchPartSite;

import com.genuitec.piplug.tools.ui.Activator;

public class RunLocallyAction extends ExtensionSelectionListenerAction {

    protected RunLocallyAction(IWorkbenchPartSite site) {
	super("Run locally", site);
	setToolTipText("Run the selected apps locally");
	setImageDescriptor(Activator.imageDescriptorFromPlugin(
		Activator.PLUGIN_ID, "resources/run.gif"));
    }

    public void run() {

    }
}
