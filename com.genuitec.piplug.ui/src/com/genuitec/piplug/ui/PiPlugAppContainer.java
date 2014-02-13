package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugAppContainer extends Composite {

    private StackLayout stackLayout;
    private IPiPlugUITheme theme;

    public PiPlugAppContainer(Composite parent, IPiPlugUITheme theme) {
	super(parent, SWT.NONE);
	this.theme = theme;
	setBackground(theme.getBackgroundColor());
	stackLayout = new StackLayout();
	stackLayout.marginHeight = stackLayout.marginWidth = 0;
	setLayout(stackLayout);
	setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    public void activate(Composite child) {
	stackLayout.topControl = child;
	layout(true);
    }

    public IPiPlugUITheme getTheme() {
	return theme;
    }
}