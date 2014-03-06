package com.genuitec.piplug.daemon;

import java.util.Timer;
import java.util.TimerTask;

public class EventNotifier {
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
	if (task != null)
	    task.cancel();
    }

    public void schedule(long delay) {
	task = new NotifyEventsTask();
	timer.schedule(task, delay);
    }

    public void waitForEvents() {
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
    }

    private void doNotify() {
	synchronized (this) {
	    notifyAll();
	}
    }

}
