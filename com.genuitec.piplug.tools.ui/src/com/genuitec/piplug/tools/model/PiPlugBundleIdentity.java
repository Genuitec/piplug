package com.genuitec.piplug.tools.model;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.osgi.framework.Version;

public class PiPlugBundleIdentity {
	private final String id;
	private final Version version;

	public PiPlugBundleIdentity(String id, Version version) {
		if (null == id)
			throw new IllegalStateException("id cannot be null");
		if (null == version)
			throw new IllegalStateException("version cannot be null");
		this.id = id;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public Version getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof PiPlugBundleIdentity))
			return false;
		PiPlugBundleIdentity other = (PiPlugBundleIdentity) obj;
		return id.equals(other.id) && version.equals(other.version);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() + version.hashCode() * 3;
	}

	public static PiPlugBundleIdentity fromPluginModelBase(IPluginModelBase plugin) {
		BundleDescription bundleDescription = plugin.getBundleDescription();
		return new PiPlugBundleIdentity(bundleDescription.getName(), bundleDescription.getVersion());
	}
}
