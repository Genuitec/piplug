package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugAppContainer extends Composite {

    private StackLayout stackLayout;
    private IPiPlugServices services;

    public PiPlugAppContainer(Composite parent, IPiPlugServices services) {
	super(parent, SWT.NONE);
	this.services = services;
	setBackground(services.getGlobalTheme().getBackgroundColor());
	stackLayout = new StackLayout();
	stackLayout.marginHeight = stackLayout.marginWidth = 0;
	setLayout(stackLayout);
	setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    public void activate(Composite child) {
	stackLayout.topControl = child;
	layout(true);
    }

    public IPiPlugUITheme getTheme() {
	return services.getGlobalTheme();
    }

    public IPiPlugServices getServices() {
	return services;
    }
}