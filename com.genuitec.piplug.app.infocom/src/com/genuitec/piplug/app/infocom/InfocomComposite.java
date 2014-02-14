package com.genuitec.piplug.app.infocom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import russotto.zplet.ZColor;
import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;

public class InfocomComposite extends Composite {

    private ZStatus statusLine;
    private ZScreen screen;

    public InfocomComposite(Composite parent) {
	super(parent, SWT.NONE);

	// setup the grid layout
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = layout.marginHeight = 30;
	layout.verticalSpacing = 8;
	setLayout(layout);
	setBackground(ZColor.getcolor(ZColor.Z_BLACK));

	// prepare the status line
	statusLine = new ZStatus(this);
	statusLine.setForeground(ZColor.getcolor(ZColor.Z_GREEN));
	statusLine.setBackground(ZColor.getcolor(ZColor.Z_BLACK));
	statusLine.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

	// prepare the emulator screen
	screen = new ZScreen(this);
	screen.setZForeground(ZColor.Z_GREEN);
	screen.setZBackground(ZColor.Z_BLACK);
	screen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	// pair up the fonts
	statusLine.setFont(screen.getFixedFont());
    }

    public ZScreen getScreen() {
	return screen;
    }

    public ZStatus getStatusLine() {
	return statusLine;
    }
}