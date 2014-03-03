package com.genuitec.piplug.tools.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import com.genuitec.piplug.tools.ui.Activator;


public class PiPlugApplicationExtension implements Comparable<PiPlugApplicationExtension>{


	private String image;
	private String name;
	private PiPlugBundle bundle;

	public PiPlugApplicationExtension(String appName, String image) {
		this.name = appName;
		this.image = image;
	}
	
	public void bind(PiPlugBundle bundle) {
		this.bundle = bundle;
	}

	public String getName() {
		return name;
	}
	
	public PiPlugBundle getBundle() {
		return bundle;
	}
	
	public String getImage() {
		return image;
	}

	public InputStream getImageData() {
		if (null == getImage())
			return null;
		
		IProject project = bundle.getProject();
		if (project == null) {
			// must be loaded from the extension registry
			Bundle osgiBundle = Platform.getBundle(bundle.getBundleId());
			if (osgiBundle == null) {
				// OK, bad data
				return null;
			}
			URL imageUrl = osgiBundle.getEntry(getImage());
			if (null == imageUrl) {
				// more bad configuration
				return null;
			}
			try {
				return imageUrl.openStream();
			} catch (IOException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Could not read from " + imageUrl, e));
				return null;
			}
		}
		
		IResource resource = project.findMember(getImage());
		if (null == resource || !(resource instanceof IFile))
			return null;
		try {
			return ((IFile)resource).getContents();
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not read image " + this.bundle.getBundleId() + "/" + getImage(), e));
			return null;
		}
	}

	@Override
	public int compareTo(PiPlugApplicationExtension o) {
		return name.compareTo(o.name);
	}

	public PiPlugDeploymentState getDeploymentState() {
		return new PiPlugDeploymentState(PiPlugDeploymentStatus.NEVER_DEPLOYED);
	}
}
