/************************************************************
 *
 * Copyright (c) 2001 Chemi. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/mit-license.html
 *
 ************************************************************/

package es.org.chemi.games.snake.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.org.chemi.games.snake.SnakePlugin;

public class Timer extends Composite implements Runnable {
    private boolean running = false;
    boolean reset = true;
    int cron = 0;
    int length = 1;
    private int maxvalue = 9;
    Label[] digits = null;

    private Composite parent = null;

    private String owner = null;

    String aux = null;

    public Timer(Composite parent, int style, int length, String owner) {
	super(parent, style);
	this.parent = parent;
	this.owner = owner;
	this.length = length;

	this.digits = new Label[length];

	SnakePlugin.trace(this.getClass().getName(),
		"Creation of the timer started. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$

	// Calculate maximun value.
	StringBuffer tmp = new StringBuffer();
	for (int i = 0; i < length; i++)
	    tmp.append(9);
	maxvalue = Integer.parseInt(tmp.toString());

	// Set layout.
	RowLayout timerLayout = new RowLayout();
	timerLayout.justify = true;
	setLayout(timerLayout);

	// Set layout.
	RowLayout containerLayout = new RowLayout();
	containerLayout.spacing = 0;
	containerLayout.marginTop = 0;
	containerLayout.marginBottom = 0;
	containerLayout.marginLeft = 0;
	containerLayout.marginRight = 0;
	this.setLayout(containerLayout);

	// Loading images.
	SnakePlugin.trace(this.getClass().getName(),
		"Loading images for the timer. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	for (int i = 0; i < 10; i++) {
	    if (SnakePlugin.getResourceManager().getImage(Integer.toString(i)) == null)
		SnakePlugin.getResourceManager().putImage(
			Integer.toString(i),
			new Image(Display.getCurrent(), this.getClass()
				.getClassLoader()
				.getResourceAsStream("icons/" + i + ".gif"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// Loading colors.
	SnakePlugin.trace(this.getClass().getName(),
		"Loading colors for the timer. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$

	// Initialice the timer.
	for (int i = 0; i < length; i++) {
	    digits[i] = new Label(this, SWT.NONE);
	    digits[i].setBackground(Display.getCurrent().getSystemColor(
		    SWT.COLOR_BLACK));
	    digits[i].setImage(SnakePlugin.getResourceManager().getImage(
		    Integer.toString(0)));
	}

	SnakePlugin.trace(this.getClass().getName(),
		"Creation of the timer finished. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void run() {
	// Set the name of the thread.
	Thread.currentThread().setName("Common - Timer"); //$NON-NLS-1$

	while (running) {
	    try {
		// Count seconds.
		Thread.sleep(1000);
		cron++;

		// Calculate the images to be displayed.
		aux = Integer.toString(cron);
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < digits.length - aux.length(); i++)
		    tmp.append(0);
		tmp.append(aux);
		aux = tmp.toString();

		if (!(cron > maxvalue)) // Don't increment.
		{
		    // Show images.
		    parent.getDisplay().syncExec(new Runnable() {
			public void run() {
			    try {
				for (int i = 0; i < digits.length; i++)
				    digits[i].setImage(SnakePlugin
					    .getResourceManager().getImage(
						    aux.substring(i, i + 1)));
			    } catch (SWTException ex) {
				// Theoretically fired when closing the game
				// while the timer still working.
			    }
			}
		    });
		}
	    } catch (InterruptedException ex) {
		SnakePlugin
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, SnakePlugin.getDefault()
				.getBundle().getSymbolicName(), IStatus.ERROR,
				ex.toString(), null));
	    } catch (SWTException ex) {
		// Theoretically fired when closing the game while the timer
		// still working.
	    }

	    // Check if it needs to reset the timer (see resetRunning method).
	    if (reset) {
		parent.getDisplay().syncExec(new Runnable() {
		    public void run() {
			for (int i = 0; i < length; i++)
			    digits[i].setImage(SnakePlugin.getResourceManager()
				    .getImage(Integer.toString(0)));
			cron = 0;
			reset = false;
		    }
		});
	    }
	}
    }

    public boolean isRunning() {
	return running;
    }

    // Start the timer.
    public void start() {
	SnakePlugin.trace(this.getClass().getName(),
		"Timer started. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	running = true;
	new Thread(this).start();
    }

    // Stop the timer
    public void stop() {
	SnakePlugin.trace(this.getClass().getName(),
		"Timer stopped. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	running = false;
    }

    // Reset the timer if it is running.
    public void resetRunning() {
	SnakePlugin.trace(this.getClass().getName(),
		"Timer reseted. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	reset = true;
    }

    // Reset the timer if it is stoped.
    public void resetStopped() {
	SnakePlugin.trace(this.getClass().getName(),
		"Timer reseted. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	for (int i = 0; i < length; i++)
	    digits[i].setImage(SnakePlugin.getResourceManager().getImage(
		    Integer.toString(0)));
	cron = 0;
    }

    // Get the time.
    public int getTime() {
	return cron;
    }
}