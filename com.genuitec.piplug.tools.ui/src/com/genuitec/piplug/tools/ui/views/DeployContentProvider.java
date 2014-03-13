package com.genuitec.piplug.tools.ui.views;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.BundleDescriptors;
import com.genuitec.piplug.tools.model.IPiPlugBundleListener;
import com.genuitec.piplug.tools.model.PiPlugBundle;
import com.genuitec.piplug.tools.model.PiPlugCore;
import com.genuitec.piplug.tools.model.PiPlugExtension;

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
		BundleDescriptors localBundleDescriptors = PiPlugCore.getInstance().getLocalBundleDescriptors();
		List<BundleDescriptor> descriptors = localBundleDescriptors.getDescriptors();
		if (null == descriptors || descriptors.isEmpty())
			return new Object[0];
		SortedSet<PiPlugExtension> extensions = new TreeSet<PiPlugExtension>();
		for (BundleDescriptor next : descriptors) {
			PiPlugBundle bundle = PiPlugCore.getInstance().getBundle(next);
			if (null == bundle)
				continue;
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
	public void bundlesChanged(BundleDescriptors localBundleDescriptor,
			BundleDescriptors remoteBundleDescriptor) {
		refreshViewer();
	}
}