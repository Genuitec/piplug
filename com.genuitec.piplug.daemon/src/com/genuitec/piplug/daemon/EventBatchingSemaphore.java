package com.genuitec.piplug.daemon;

import java.util.Timer;
import java.util.TimerTask;

import com.genuitec.piplug.common.BundleDescriptors;

public class EventBatchingSemaphore {
    public class NotifyEventsTask extends TimerTask {
	@Override
	public void run() {
	    if (this != task)
		return;
	    task = null;
	    doNotify();
	}
    }

    private Timer timer = new Timer();
    private NotifyEventsTask task;
    private BundleDescriptors descriptors;
    private boolean debug;

    public EventBatchingSemaphore(BundleDescriptors descriptors, boolean debug) {
	this.descriptors = descriptors;
	this.debug = debug;
    }

    public void cancel() {
	if (task != null) {
	    NotifyEventsTask oldTask = task;
	    task = null;
	    oldTask.cancel();
	}
    }

    public void schedule(long delay) {
	task = new NotifyEventsTask();
	timer.schedule(task, delay);
    }

    public void canReturnEvents(long clientSyncTime) {
	if (clientSyncTime == descriptors.getSyncTime()) {
	    synchronized (this) {
		while (clientSyncTime == descriptors.getSyncTime()) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
	    }
	}
    }

    private void doNotify() {
	synchronized (this) {
	    descriptors.setSyncTime(System.currentTimeMillis());
	    if (debug)
		System.out.println("Daemon sync time: "
			+ descriptors.getSyncTime());
	    notifyAll();
	}
    }

}
