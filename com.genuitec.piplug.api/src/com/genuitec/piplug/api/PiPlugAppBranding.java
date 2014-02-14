package com.genuitec.piplug.api;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class PiPlugAppBranding implements IPiPlugAppBranding {

    private String bundleID;
    private String appName;
    private Image image64;
    private Image image128;

    public PiPlugAppBranding(String bundleID, String appName) {
	this.bundleID = bundleID;
	this.appName = appName;
    }

    @Override
    public Image getImage64() {
	if (image64 == null) {
	    URL url = Platform.getBundle(bundleID).getEntry("app64.png");
	    image64 = ImageDescriptor.createFromURL(url).createImage(false);
	}
	return image64;
    }

    @Override
    public Image getImage128() {
	if (image128 == null) {
	    URL url = Platform.getBundle(bundleID).getEntry("app128.png");
	    image128 = ImageDescriptor.createFromURL(url).createImage(false);
	}
	return image128;
    }

    @Override
    public String getName() {
	return appName;
    }
}
