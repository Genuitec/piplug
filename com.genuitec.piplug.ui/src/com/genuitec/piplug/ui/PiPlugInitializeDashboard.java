package com.genuitec.piplug.ui;

public class PiPlugInitializeDashboard implements Runnable {

    private PiPlugAppContainer container;

    public PiPlugInitializeDashboard(PiPlugAppContainer container) {
	this.container = container;
    }

    @Override
    public void run() {
	PiPlugDashboardComposite dashboard = new PiPlugDashboardComposite(
		container, container.getTheme());
	container.activate(dashboard);
    }
}