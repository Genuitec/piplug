package com.genuitec.piplug.ui;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugDashboardComposite extends Composite {

    private final class CloseShellListener extends SelectionAdapter {
	@Override
	public void widgetSelected(SelectionEvent arg0) {
	    getShell().dispose();
	}
    }

    private static final String LOGO = "images/PiPlug-wText-50h.png";
    private Composite buttonsArea;
    PiPlugAppContainer container;

    public PiPlugDashboardComposite(PiPlugAppContainer container,
	    Set<IPiPlugApplication> applications) {
	super(container, SWT.NONE);
	this.container = container;

	IPiPlugUITheme theme = container.getTheme();
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = 32;
	layout.marginHeight = 26;
	setLayout(layout);
	setBackground(theme.getBackgroundColor());
	Label label = new Label(this, SWT.CENTER);
	label.setImage(PiPlugUIActivator.loadImage(LOGO));
	label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
	label.setBackground(theme.getBackgroundColor());

	buttonsArea = new Composite(this, SWT.NONE);
	buttonsArea.setBackground(theme.getBackgroundColor());
	layout = new GridLayout(applications.size(), false);
	layout.marginWidth = layout.marginHeight = 0;
	layout.horizontalSpacing = 40;
	buttonsArea.setLayout(layout);
	GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
	int size = applications.size();
	gd.widthHint = (size * 128) + ((size - 1) * 40);
	gd.heightHint = 160;
	buttonsArea.setLayoutData(gd);

	for (IPiPlugApplication next : applications)
	    new PiPlugAppHandle(buttonsArea, next, theme);

	Button button = new Button(this, SWT.NONE);
	button.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
	button.setText("Close PiPlug");
	button.addSelectionListener(new CloseShellListener());
	button.setBackground(theme.getBackgroundColor());
    }

    private class PiPlugAppHandle extends Composite implements MouseListener {

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
	    button.setImage(app.getBranding().getImage128());
	    GridData gd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
	    gd.heightHint = 128;
	    gd.widthHint = 128;
	    button.setLayoutData(gd);
	    button.addMouseListener(this);
	    label = new Label(this, SWT.CENTER);
	    label.setText(app.getBranding().getName());
	    label.setFont(theme.getSubtitleFont());
	    label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
	    label.addMouseListener(this);
	    addMouseListener(this);
	    gd = new GridData(SWT.FILL, SWT.FILL);
	    gd.widthHint = 128;
	    gd.heightHint = 154;
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
	    if (child == null)
		child = app.prepare(container.getServices(), container);
	    container.activate(child);
	    app.resume(container.getServices());
	}

	@Override
	public void mouseUp(MouseEvent arg0) {
	    // nothing to do
	}
    }
}