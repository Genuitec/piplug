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

@SuppressWarnings("restriction")
public class PiPlugBundle implements Comparable<PiPlugBundle> {
	private Set<PiPlugApplicationExtension> apps = new HashSet<PiPlugApplicationExtension>();
	private IProject project;
	private PiPlugBundleIdentity identity;
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
		this.identity = PiPlugBundleIdentity.fromPluginModelBase(plugin);
	}

	public String getBundleId() {
		return identity.getId();
	}

	public SortedSet<PiPlugApplicationExtension> getApps() {
		return new TreeSet<PiPlugApplicationExtension>(apps);
	}

	public void addApplication(PiPlugApplicationExtension app) {
		apps.add(app);
		app.bind(this);
	}

	public IProject getProject() {
		return project;
	}

	public PiPlugBundleIdentity getIdentity() {
		return identity;
	}

	public void setApplications(Set<PiPlugApplicationExtension> bundleApps) {
		if (null == bundleApps)
			bundleApps = new HashSet<PiPlugApplicationExtension>();
		this.apps = bundleApps;
		for (PiPlugApplicationExtension app : bundleApps) {
			app.bind(this);
		}
	}

	@Override
	public int compareTo(PiPlugBundle o) {
		return identity.compareTo(o.identity);
	}

	public File getArtifact() {
		return artifact;
	}

	public IPluginModelBase getPlugin() {
		return plugin;
	}
}
