package com.genuitec.piplug.tools.ui.views;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.SelectionListenerAction;

import com.genuitec.piplug.tools.model.PiPlugExtension;

public class ExtensionSelectionListenerAction extends SelectionListenerAction {
	public ExtensionSelectionListenerAction(String label, IWorkbenchPartSite site) {
		super(label);
		site.getSelectionProvider().addSelectionChangedListener(this);
		selectionChanged((IStructuredSelection) site.getSelectionProvider().getSelection());
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return !selection.isEmpty();
	}
	
	protected Set<PiPlugExtension> getSelectedExtensions() {
		Set<PiPlugExtension> extensions = new HashSet<PiPlugExtension>();
		for(Object object : getSelectedNonResources()) {
			if (object instanceof PiPlugExtension) {
				extensions.add((PiPlugExtension) object);
			}
		}
		return extensions;
	}

}
