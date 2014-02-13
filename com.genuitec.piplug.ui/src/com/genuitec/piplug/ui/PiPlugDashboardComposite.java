package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugDashboardComposite extends Composite {

    private final class CloseShellListener extends SelectionAdapter {
	@Override
	public void widgetSelected(SelectionEvent arg0) {
	    getShell().dispose();
	}
    }

    private static final String LOGO = "images/PiPlug-wText-50h.png";

    public PiPlugDashboardComposite(Composite parent, IPiPlugUITheme theme) {
	super(parent, SWT.NONE);
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = 30;
	layout.marginHeight = 26;
	setLayout(layout);
	setBackground(theme.getBackgroundColor());
	Label label = new Label(this, SWT.CENTER);
	label.setImage(PiPlugUIActivator.loadImage(LOGO));
	label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
	label.setBackground(theme.getBackgroundColor());
	Button button = new Button(this, SWT.NONE);
	button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true));
	button.setText("Close PiPlug");
	button.addSelectionListener(new CloseShellListener());
	button.setBackground(theme.getBackgroundColor());
    }
}
