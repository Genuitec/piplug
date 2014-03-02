/************************************************************
 *
 * Copyright (c) 2003 Chemi. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/mit-license.html
 *
 ************************************************************/

package es.org.chemi.games.snake.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.org.chemi.games.snake.SnakePlugin;

public class Counter extends Composite {
    int length = 1;
    private int value = 0;
    private int maxvalue = 9;
    private Label[] digits = null;

    Composite parent = null;

    private String owner = null;

    public Counter(Composite parent, int style, int length, int value,
	    String owner) {
	super(parent, style);
	this.parent = parent;
	this.length = length;
	this.value = value;
	this.owner = owner;

	this.digits = new Label[length];

	SnakePlugin.trace(this.getClass().getName(),
		"Creation of the counter started. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$

	// Calculate maximun value.
	StringBuffer tmp = new StringBuffer();
	for (int i = 0; i < length; i++)
	    tmp.append(9);
	maxvalue = Integer.parseInt(tmp.toString());

	// Set layout.
	RowLayout counterLayout = new RowLayout();
	counterLayout.justify = true;
	counterLayout.spacing = 0;
	counterLayout.marginTop = 0;
	counterLayout.marginBottom = 0;
	counterLayout.marginLeft = 0;
	counterLayout.marginRight = 0;
	this.setLayout(counterLayout);

	// Loading images.
	SnakePlugin.trace(this.getClass().getName(),
		"Loading images for the counter. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
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
		"Loading colors for the counter. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$		

	// Initialice the counter.
	for (int i = 0; i < length; i++) {
	    digits[i] = new Label(this, SWT.NONE);
	    digits[i].setBackground(Display.getCurrent().getSystemColor(
		    SWT.COLOR_BLACK));
	}
	drawCounter(value);
	SnakePlugin.trace(this.getClass().getName(),
		"Creation of the counter finished. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void drawCounter(int param) {
	SnakePlugin.trace(this.getClass().getName(),
		"Refreshing the counter. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$

	// Calculate the images to be displayed.
	String aux = Integer.toString(param);
	StringBuffer tmp = new StringBuffer();
	for (int i = 0; i < digits.length - aux.length(); i++)
	    tmp.append(0);
	tmp.append(aux);
	aux = tmp.toString();

	// Show images.
	for (int i = 0; i < digits.length; i++)
	    digits[i].setImage(SnakePlugin.getResourceManager().getImage(
		    aux.substring(i, i + 1)));
    }

    public void increase() {
	SnakePlugin.trace(this.getClass().getName(),
		"Increasing the counter 1 unit. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	value += 1;
	if (!(value > maxvalue))
	    drawCounter(value);
    }

    public void increase(int param) {
	SnakePlugin.trace(this.getClass().getName(),
		"Increasing the counter " + param + " units. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	value += param;
	if (value > maxvalue)
	    drawCounter(maxvalue);
	else
	    drawCounter(value);
    }

    public void decrease() {
	SnakePlugin.trace(this.getClass().getName(),
		"Decreasing the counter 1 unit. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	value -= 1;
	drawCounter(value);
    }

    public void reset() {
	SnakePlugin.trace(this.getClass().getName(),
		"Reseting the counter. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	value = 0;
	drawCounter(value);
    }

    public void reset(int value) {
	this.value = value;
	drawCounter(value);
    }

    public int getValue() {
	return value;
    }

    public boolean isZero() {
	if (value == 0)
	    return true;
	else
	    return false;
    }
}