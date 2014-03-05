package com.genuitec.piplug.tools.ui.views;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.SelectionListenerAction;

import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;

public class PiPlugSelectionListenerAction extends SelectionListenerAction {
	public PiPlugSelectionListenerAction(String label, IWorkbenchPartSite site) {
		super(label);
		site.getSelectionProvider().addSelectionChangedListener(this);
		selectionChanged((IStructuredSelection) site.getSelectionProvider().getSelection());
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return !selection.isEmpty();
	}
	
	protected Set<PiPlugApplicationExtension> getSelectedApps() {
		Set<PiPlugApplicationExtension> apps = new HashSet<PiPlugApplicationExtension>();
		for(Object object : getSelectedNonResources()) {
			if (object instanceof PiPlugApplicationExtension) {
				apps.add((PiPlugApplicationExtension) object);
			}
		}
		return apps;
	}

}
