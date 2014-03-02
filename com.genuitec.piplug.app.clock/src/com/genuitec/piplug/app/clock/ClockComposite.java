package com.genuitec.piplug.app.clock;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;

import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class ClockComposite extends Composite {

    public class GoHomeListener extends MouseAdapter {
	@Override
	public void mouseDown(MouseEvent e) {
	    services.switchToHome();
	}
    }

    private IPiPlugServices services;
    private ClockFace face;

    public ClockComposite(IPiPlugServices services, Composite parent) {
	super(parent, SWT.NONE);
	setBackground(new Color(getDisplay(), 255, 255, 255));
	this.services = services;

	IPiPlugUITheme theme = services.getGlobalTheme();
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = 48;
	layout.marginHeight = 48;
	setLayout(layout);
	setBackground(theme.getBackgroundColor());
	Label logo = new Label(this, SWT.CENTER);
	logo.setImage(theme.getHeaderLogoImage());
	logo.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
	logo.setBackground(theme.getBackgroundColor());

	face = new ClockFace(this);
	face.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	Label button = new Label(this, SWT.NONE);
	button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
	Bundle bundle = Platform.getBundle("com.genuitec.piplug.app.clock");
	URL url = bundle.getEntry("images/icon-close64.png");
	button.setImage(ImageDescriptor.createFromURL(url).createImage());
	button.addMouseListener(new GoHomeListener());
	button.setBackground(theme.getBackgroundColor());

	updateTime();
    }

    public void updateTime() {
	face.updateTime();
    }
}