package com.genuitec.piplug.ui;

import java.util.Set;

import com.genuitec.piplug.api.IPiPlugApplication;

public class PiPlugInitializeDashboard implements Runnable {

    private PiPlugAppContainer container;
    private Set<IPiPlugApplication> applications;

    public PiPlugInitializeDashboard(Set<IPiPlugApplication> applications,
	    PiPlugAppContainer container) {
	this.container = container;
	this.applications = applications;
    }

    @Override
    public void run() {
	PiPlugDashboardComposite dashboard = new PiPlugDashboardComposite(
		container, applications);
	container.setHome(dashboard);
	container.activate(dashboard);
    }
}