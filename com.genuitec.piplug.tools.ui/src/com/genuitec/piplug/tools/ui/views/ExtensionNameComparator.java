package com.genuitec.piplug.tools.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.genuitec.piplug.tools.model.PiPlugExtension;

public class ExtensionNameComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		PiPlugExtension a1 = (PiPlugExtension) e1;
		PiPlugExtension a2 = (PiPlugExtension) e2;
		return a1.compareTo(a2);
	}
}
