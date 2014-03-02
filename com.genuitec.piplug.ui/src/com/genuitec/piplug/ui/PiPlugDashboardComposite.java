package com.genuitec.piplug.ui;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugDashboardComposite extends Composite {

    private final class CloseShellListener extends MouseAdapter {
	@Override
	public void mouseDown(MouseEvent e) {
	    getShell().dispose();
	}
    }

    private Composite buttonsArea;
    PiPlugAppContainer container;
    public PiPlugAppHandle runningApp;

    public PiPlugDashboardComposite(PiPlugAppContainer container,
	    Set<IPiPlugApplication> applications) {
	super(container, SWT.NONE);
	this.container = container;

	IPiPlugUITheme theme = container.getTheme();
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = 48;
	layout.marginHeight = 48;
	setLayout(layout);
	setBackground(theme.getBackgroundColor());
	Label label = new Label(this, SWT.CENTER);
	label.setImage(theme.getHeaderLogoImage());
	label.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
	label.setBackground(theme.getBackgroundColor());

	buttonsArea = new Composite(this, SWT.NONE);
	buttonsArea.setBackground(theme.getBackgroundColor());
	layout = new GridLayout(Math.min(4, applications.size()), false);
	layout.marginWidth = layout.marginHeight = 0;
	layout.horizontalSpacing = 40;
	layout.verticalSpacing = 40;
	buttonsArea.setLayout(layout);
	GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
	int size = layout.numColumns;
	gd.widthHint = (size * 256) + ((size - 1) * 40);
	buttonsArea.setLayoutData(gd);

	for (IPiPlugApplication next : applications)
	    new PiPlugAppHandle(buttonsArea, next, theme);

	Label quit = new Label(this, SWT.NONE);
	gd = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false);
	gd.widthHint = 48;
	gd.heightHint = 48;
	quit.setLayoutData(gd);
	quit.setImage(PiPlugUIActivator.loadImage("images/icon-quit48.png"));
	quit.addMouseListener(new CloseShellListener());
	quit.setBackground(theme.getBackgroundColor());
    }

    private class PiPlugAppHandle extends Composite implements MouseListener,
	    Runnable {

	private IPiPlugApplication app;
	private Label label;
	private Composite child;

	public PiPlugAppHandle(Composite parent, IPiPlugApplication app,
		IPiPlugUITheme theme) {
	    super(parent, SWT.NONE);
	    this.app = app;
	    createControls(theme);
	    app.installed(container.getServices());
	}

	private void createControls(IPiPlugUITheme theme) {
	    GridLayout layout = new GridLayout(1, false);
	    layout.marginWidth = layout.marginHeight = 0;
	    layout.verticalSpacing = 10;
	    setLayout(layout);
	    Label button = new Label(this, SWT.PUSH);
	    button.setImage(app.getBranding().getImage());
	    button.setBackground(theme.getBackgroundColor());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    gd.heightHint = 256;
	    gd.widthHint = 166;
	    button.setLayoutData(gd);
	    button.addMouseListener(this);
	    label = new Label(this, SWT.CENTER);
	    label.setText(app.getBranding().getName());
	    label.setFont(theme.getTitleFont());
	    label.setForeground(theme.getTitleColor());
	    label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	    label.addMouseListener(this);
	    label.setBackground(theme.getBackgroundColor());
	    addMouseListener(this);
	    gd = new GridData(SWT.FILL, SWT.FILL);
	    gd.widthHint = 256;
	    gd.heightHint = 206;
	    setLayoutData(gd);
	    this.setBackground(theme.getBackgroundColor());
	}

	@Override
	public void mouseDoubleClick(MouseEvent arg0) {
	    // nothing to do
	}

	@Override
	public void mouseDown(MouseEvent arg0) {
	    label.setText("Loading...");
	    getDisplay().asyncExec(this);
	}

	public void run() {
	    if (child == null)
		child = app.prepare(container.getServices(), container);
	    container.activate(child);
	    label.setText(app.getBranding().getName());
	    PiPlugDashboardComposite.this.runningApp = this;
	    app.resume(container.getServices());
	}

	public void suspend() {
	    app.suspend(container.getServices());
	}

	@Override
	public void mouseUp(MouseEvent arg0) {
	    // nothing to do
	}
    }

    public void suspendRunningApp() {
	if (runningApp != null)
	    runningApp.suspend();
    }
}