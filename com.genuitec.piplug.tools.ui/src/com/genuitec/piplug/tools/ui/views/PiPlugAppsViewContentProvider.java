package com.genuitec.piplug.tools.ui.views;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.genuitec.piplug.tools.model.IPiPlugApplicationListener;
import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;
import com.genuitec.piplug.tools.model.PiPlugBundle;
import com.genuitec.piplug.tools.model.PiPlugCore;

public class PiPlugAppsViewContentProvider implements IStructuredContentProvider,
		IPiPlugApplicationListener {

	public static class RefreshRunnable implements Runnable {

		private Viewer viewer;
		private PiPlugAppsView view;

		public RefreshRunnable(Viewer viewer, PiPlugAppsView view) {
			this.viewer = viewer;
			this.view = view;
		}

		@Override
		public void run() {
			viewer.refresh();
			view.packColumns();
		}

	}

	private Viewer viewer;
	
	private PiPlugAppsView view;
	
	public PiPlugAppsViewContentProvider(PiPlugAppsView view) {
		this.view = view;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		this.viewer = v;
		PiPlugCore.getInstance().addPiPlugApplicationListener(this);
	}

	public void dispose() {
		PiPlugCore.getInstance().removePiPlugApplicationListener(this);
	}

	public Object[] getElements(Object parent) {
		Set<PiPlugBundle> bundles = PiPlugCore.getInstance().getBundles();
		if (null == bundles)
			return new Object[0];
		SortedSet<PiPlugApplicationExtension> applications = new TreeSet<PiPlugApplicationExtension>();
		for (PiPlugBundle bundle : bundles) {
			SortedSet<PiPlugApplicationExtension> apps = bundle.getApps();
			if (null != apps)
				applications.addAll(apps);
		}
		return applications.toArray();
	}

	private void refreshViewer() {
		Control control = viewer.getControl();
		if (null == control || control.isDisposed())
			return;
		Display display = control.getDisplay();
		if (null == display || display.isDisposed())
			return;
		display.asyncExec(new PiPlugAppsViewContentProvider.RefreshRunnable(viewer, view));
	}
	
	@Override
	public void bundleAdded(PiPlugBundle bundle) {
		refreshViewer();
	}

	@Override
	public void bundleChanged(PiPlugBundle bundle) {
		refreshViewer();
	}

	@Override
	public void bundleRemoved(PiPlugBundle bundle) {
		refreshViewer();
	}
}