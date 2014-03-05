package com.genuitec.piplug.ui;

import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.client.PiPlugClient;

public class PiPlugInitializeDashboard implements Runnable {

    private PiPlugAppContainer container;
    private Set<IPiPlugApplication> applications;
    private List<Bundle> loadedBundles;
    private PiPlugClient client;

    public PiPlugInitializeDashboard(Set<IPiPlugApplication> applications,
	    PiPlugAppContainer container, List<Bundle> loadedBundles,
	    PiPlugClient client) {
	this.container = container;
	this.applications = applications;
	this.loadedBundles = loadedBundles;
	this.client = client;
    }

    @Override
    public void run() {
	PiPlugDashboardComposite dashboard = new PiPlugDashboardComposite(
		container, applications);
	container.setHome(dashboard);
	container.activate(dashboard);
	container.setLoadedBundles(loadedBundles);
	container.setClient(client);
    }
}