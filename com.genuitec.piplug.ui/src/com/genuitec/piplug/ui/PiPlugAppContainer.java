package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugAppContainer extends Composite {

    public class SuspendAppRunnable implements Runnable {
	@Override
	public void run() {
	    home.suspendRunningApp();
	}
    }

    private StackLayout stackLayout;
    private IPiPlugServices services;
    private PiPlugDashboardComposite home;

    public PiPlugAppContainer(Composite parent, PiPlugServices services) {
	super(parent, SWT.NONE);
	services.setContainer(this);
	this.services = services;
	setBackground(services.getGlobalTheme().getBackgroundColor());
	stackLayout = new StackLayout();
	stackLayout.marginHeight = stackLayout.marginWidth = 0;
	setLayout(stackLayout);
	setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    public void setHome(PiPlugDashboardComposite home) {
	this.home = home;
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

    public void switchToHome() {
	activate(home);
	getDisplay().asyncExec(new SuspendAppRunnable());
    }
}