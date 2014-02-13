package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugStartingUpComposite extends Composite {

    private static final String LOGO = "images/PiPlug-LogoOnly-200w.png";
    private Label progress;

    public PiPlugStartingUpComposite(Composite parent, IPiPlugUITheme theme) {
	super(parent, SWT.NONE);
	setLayout(new GridLayout(1, false));
	setBackground(theme.getBackgroundColor());
	Label label = new Label(this, SWT.CENTER);
	label.setImage(PiPlugUIActivator.loadImage(LOGO));
	label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true));
	label.setBackground(theme.getBackgroundColor());
	this.progress = new Label(this, SWT.CENTER);
	this.progress.setText("Initializing...");
	this.progress.setFont(theme.getSubtitleFont());
	this.progress.setForeground(theme.getSubtitleColor());
	this.progress.setBackground(theme.getBackgroundColor());
	GridData gd = new GridData(SWT.CENTER, SWT.TOP, true, true);
	gd.widthHint = 300;
	this.progress.setLayoutData(gd);
    }

    public void setMessage(String message) {
	this.progress.setText(message);
    }
}
