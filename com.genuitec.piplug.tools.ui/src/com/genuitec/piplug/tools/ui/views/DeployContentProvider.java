package com.genuitec.piplug.tools.ui.views;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.genuitec.piplug.tools.model.IPiPlugBundleListener;
import com.genuitec.piplug.tools.model.PiPlugExtension;
import com.genuitec.piplug.tools.model.PiPlugBundle;
import com.genuitec.piplug.tools.model.PiPlugCore;

public class DeployContentProvider implements IStructuredContentProvider,
		IPiPlugBundleListener {

	public static class RefreshRunnable implements Runnable {

		private Viewer viewer;
		private DeployView view;

		public RefreshRunnable(Viewer viewer, DeployView view) {
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
	
	private DeployView view;
	
	public DeployContentProvider(DeployView view) {
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
		Set<PiPlugBundle> bundles = PiPlugCore.getInstance().getLocalBundles();
		if (null == bundles)
			return new Object[0];
		SortedSet<PiPlugExtension> extensions = new TreeSet<PiPlugExtension>();
		for (PiPlugBundle bundle : bundles) {
			SortedSet<PiPlugExtension> bundleExtensions = bundle.getExtensions();
			if (null != extensions)
				extensions.addAll(bundleExtensions);
		}
		return extensions.toArray();
	}

	private void refreshViewer() {
		Control control = viewer.getControl();
		if (null == control || control.isDisposed())
			return;
		Display display = control.getDisplay();
		if (null == display || display.isDisposed())
			return;
		display.asyncExec(new DeployContentProvider.RefreshRunnable(viewer, view));
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