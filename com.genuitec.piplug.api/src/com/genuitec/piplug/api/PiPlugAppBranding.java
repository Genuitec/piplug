package com.genuitec.piplug.api;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class PiPlugAppBranding implements IPiPlugAppBranding {

    private String bundleID;
    private String appName;
    private Image image;

    public PiPlugAppBranding(String bundleID, String appName) {
	this.bundleID = bundleID;
	this.appName = appName;
    }

    @Override
    public Image getImage() {
	if (image == null) {
	    Bundle bundle = Platform.getBundle(bundleID);
	    URL url = bundle.getEntry("app.png");
	    image = ImageDescriptor.createFromURL(url).createImage(false);
	}
	return image;
    }

    @Override
    public String getName() {
	return appName;
    }
}
