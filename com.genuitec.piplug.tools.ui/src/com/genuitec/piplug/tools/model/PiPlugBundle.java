package com.genuitec.piplug.tools.model;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;

import com.genuitec.piplug.client.BundleDescriptor;

@SuppressWarnings("restriction")
public class PiPlugBundle {
	private Set<PiPlugExtension> extensions = new HashSet<PiPlugExtension>();
	private IProject project;
	private BundleDescriptor descriptor;
	private File artifact;
	private IPluginModelBase plugin;

	public PiPlugBundle(String bundleId) {
		PluginModelManager modelManager = PDECore.getDefault()
				.getModelManager();
		plugin = modelManager.findModel(bundleId);
		if (null == plugin)
			throw new IllegalStateException("Could not locate resource for "
					+ bundleId);

		IResource resource = plugin.getUnderlyingResource();
		if (null != resource)
			this.project = resource.getProject();
		this.descriptor = PiPlugCore.fromPluginModelBase(plugin);
	}

	public SortedSet<PiPlugExtension> getExtensions() {
		return new TreeSet<PiPlugExtension>(extensions);
	}

	public void addExtension(PiPlugExtension extension) {
		extensions.add(extension);
		extension.bind(this);
	}

	public IProject getProject() {
		return project;
	}

	public BundleDescriptor getDescriptor() {
		return descriptor;
	}

	public void setApplications(Set<PiPlugExtension> bundleApps) {
		if (null == bundleApps)
			bundleApps = new HashSet<PiPlugExtension>();
		this.extensions = bundleApps;
		for (PiPlugExtension app : bundleApps) {
			app.bind(this);
		}
	}

	public File getArtifact() {
		return artifact;
	}

	public IPluginModelBase getPlugin() {
		return plugin;
	}

	public void setDescriptor(BundleDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}
