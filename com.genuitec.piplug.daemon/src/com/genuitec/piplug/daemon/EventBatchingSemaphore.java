package com.genuitec.piplug.daemon;

import java.util.Timer;
import java.util.TimerTask;

public class EventBatchingSemaphore {
    public class NotifyEventsTask extends TimerTask {
	@Override
	public void run() {
	    task = null;
	    notified = true;
	    doNotify();
	}
    }

    private Timer timer = new Timer();
    private NotifyEventsTask task;
    private boolean notified;

    public void cancel() {
	if (task != null) {
	    task.cancel();
	    task = null;
	}
    }

    public void schedule(long delay) {
	notified = false;
	task = new NotifyEventsTask();
	timer.schedule(task, delay);
    }

    public void canReturnEvents() {
	while (!notified) {
	    synchronized (this) {
		if (!notified) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
	    }
	}
	notified = false;
    }

    private void doNotify() {
	synchronized (this) {
	    notifyAll();
	}
    }

}
