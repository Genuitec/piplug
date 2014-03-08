package com.genuitec.piplug.ui;

public class ContainerStatusLine implements IStatusLine {

    public class UpdateContainerStatusRunnable implements Runnable {

	private String message;

	public UpdateContainerStatusRunnable(String message) {
	    this.message = message;
	}

	@Override
	public void run() {
	    container.setStatus(message);
	}

    }

    private PiPlugAppContainer container;

    public ContainerStatusLine(PiPlugAppContainer container) {
	this.container = container;
    }

    @Override
    public void updateMessage(String message) {
	container.getDisplay().syncExec(
		new UpdateContainerStatusRunnable(message));
    }

}
