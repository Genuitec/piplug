package com.genuitec.piplug.ui;

import java.util.Map;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;

public class PiPlugInitializeDashboard implements Runnable {

    private PiPlugAppContainer container;
    private Map<BundleDescriptor, IPiPlugApplication> applications;
    private PiPlugClient client;

    public PiPlugInitializeDashboard(
	    Map<BundleDescriptor, IPiPlugApplication> applications,
	    PiPlugAppContainer container, PiPlugClient client) {
	this.container = container;
	this.applications = applications;
	this.client = client;
    }

    @Override
    public void run() {
	PiPlugDashboardComposite dashboard = new PiPlugDashboardComposite(
		container, applications);
	container.setHome(dashboard);
	container.activate(dashboard);
	container.setClient(client);
    }
}