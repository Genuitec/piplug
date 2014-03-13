package com.genuitec.piplug.tools.ui.views;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.genuitec.piplug.tools.model.PiPlugExtension;
import com.genuitec.piplug.tools.ui.Activator;

public class ExtensionNameLabelProvider extends ColumnLabelProvider {

    private ImageRegistry imageRegistry;

    public ExtensionNameLabelProvider(ImageRegistry imageRegistry) {
	this.imageRegistry = imageRegistry;
    }

    public String getText(Object obj) {
	if (obj instanceof PiPlugExtension) {
	    PiPlugExtension extension = (PiPlugExtension) obj;
	    return extension.getName();
	}
	return getText(obj);
    }

    public Image getImage(Object obj) {
	if (obj instanceof PiPlugExtension) {
	    PiPlugExtension extension = (PiPlugExtension) obj;
	    String imageKey = getImageKey(extension);
	    Image image = imageRegistry.get(imageKey);
	    if (null == image) {
		image = loadImage(extension, imageKey);
	    }
	    return image;
	}
	return null;
    }

    private Image loadImage(PiPlugExtension extension, String imageKey) {
	InputStream imageData = extension.getImageData();
	if (null == imageData)
	    return null;

	try {
	    Image image = new Image(Display.getCurrent(), imageData);
	    Image resized = resize(image, 16, 16);
	    imageRegistry.put(imageKey, resized);
	    return resized;
	} catch (Exception e) {
	    Activator
		    .getDefault()
		    .getLog()
		    .log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
			    "Could not load " + extension.getImage(), e));
	    return null;
	} finally {
	    try {
		imageData.close();
	    } catch (IOException e) {
		Activator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
				"Could not close stream", e));
	    }
	}
    }

    private String getImageKey(PiPlugExtension extension) {
	return extension.getBundle().getDescriptor().getBundleID() + "/"
		+ extension.getImage();
    }

    private Image resize(Image image, int width, int height) {
	Image scaled = new Image(Display.getDefault(), width, height);
	GC gc = new GC(scaled);
	gc.setAntialias(SWT.ON);
	gc.setInterpolation(SWT.HIGH);
	gc.drawImage(image, 0, 0, image.getBounds().width,
		image.getBounds().height, 0, 0, width, height);
	gc.dispose();
	image.dispose();
	return scaled;
    }
}