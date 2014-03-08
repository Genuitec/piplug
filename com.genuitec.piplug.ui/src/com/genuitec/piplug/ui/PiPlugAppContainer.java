package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.IPiPlugUITheme;
import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.ui.PiPlugDashboardComposite.PiPlugAppHandle;

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
    private PiPlugClient client;

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

    public void setClient(PiPlugClient client) {
	this.client = client;
    }

    public IStatusLine getStatusLine() {
	return new ContainerStatusLine(this);
    }

    public PiPlugAppHandle findAppHandle(BundleDescriptor next) {
	if (home == null)
	    return null;
	return home.findAppHandle(next);
    }

    public void setStatus(String message) {
	System.out.println(message);
    }

    public PiPlugDashboardComposite getHome() {
	return home;
    }

}