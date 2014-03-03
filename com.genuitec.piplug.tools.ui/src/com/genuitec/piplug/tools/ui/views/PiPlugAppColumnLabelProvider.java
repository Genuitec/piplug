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

import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;
import com.genuitec.piplug.tools.ui.Activator;

public class PiPlugAppColumnLabelProvider extends ColumnLabelProvider {

	private ImageRegistry imageRegistry;

	public PiPlugAppColumnLabelProvider(ImageRegistry imageRegistry) {
		this.imageRegistry = imageRegistry;
	}

	public String getText(Object obj) {
		if (obj instanceof PiPlugApplicationExtension) {
			PiPlugApplicationExtension app = (PiPlugApplicationExtension) obj;
			return app.getName();
		}
		return getText(obj);
	}

	public Image getImage(Object obj) {
		if (obj instanceof PiPlugApplicationExtension) {
			PiPlugApplicationExtension app = (PiPlugApplicationExtension) obj;
			String imageKey = getImageKey(app);
			Image image = imageRegistry.get(imageKey);
			if (null == image) {
				image = loadImage(app, imageKey);
			}
			return image;
		}
		return null;
	}

	private Image loadImage(PiPlugApplicationExtension app, String imageKey) {
		InputStream imageData = app.getImageData();
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
							"Could not load " + app.getImage(), e));
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

	private String getImageKey(PiPlugApplicationExtension app) {
		return app.getBundle().getBundleId() + "/" + app.getImage();
	}

	private Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose(); // don't forget about me!
		return scaled;
	}
}