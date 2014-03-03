package com.genuitec.piplug.tools.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;

public class PiPlugAppsComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		PiPlugApplicationExtension a1 = (PiPlugApplicationExtension) e1;
		PiPlugApplicationExtension a2 = (PiPlugApplicationExtension) e2;
		return a1.compareTo(a2);
	}
}
