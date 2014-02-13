package com.genuitec.piplug.ui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class PiPlugUIActivator implements BundleActivator {

    private static BundleContext context;
    private static ImageRegistry registry;

    static BundleContext getContext() {
	return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bundleContext) throws Exception {
	PiPlugUIActivator.context = bundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
	registry.dispose();
	PiPlugUIActivator.context = null;
    }

    /**
     * Returns the image in the piplug.ui bundle.
     * 
     * @param imagePath
     *            the path in the bundle
     * @return the image loaded (and cached for fast access)
     */
    public static Image loadImage(String imagePath) {
	if (registry == null)
	    registry = new ImageRegistry();
	Image image = registry.get(imagePath);
	if (image == null) {
	    URL url = context.getBundle().getEntry(imagePath);
	    registry.put(imagePath, ImageDescriptor.createFromURL(url));
	    image = registry.get(imagePath);
	}
	return image;
    }
}
