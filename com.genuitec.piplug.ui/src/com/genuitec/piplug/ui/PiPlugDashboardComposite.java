package com.genuitec.piplug.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.api.IPiPlugUITheme;
import com.genuitec.piplug.client.BundleDescriptor;

public class PiPlugDashboardComposite extends Composite {

    public class FindAppHandleRunnable implements Runnable {

	private BundleDescriptor descriptor;
	private PiPlugAppHandle appHandle;

	public FindAppHandleRunnable(BundleDescriptor descriptor) {
	    this.descriptor = descriptor;
	}

	@Override
	public void run() {
	    Control[] appHandles = buttonsArea.getChildren();
	    for (Control control : appHandles) {
		if (control instanceof PiPlugAppHandle) {
		    PiPlugAppHandle handle = (PiPlugAppHandle) control;
		    if (descriptor.equals(handle.getDescriptor())) {
			this.appHandle = handle;
			return;
		    }
		}
	    }
	}

	public PiPlugAppHandle getAppHandle() {
	    return appHandle;
	}

    }

    private final class AppHandleComparator implements Comparator<Control> {
	@Override
	public int compare(Control o1, Control o2) {
	    return getText(o1).compareTo(getText(o2));
	}

	private String getText(Control o1) {
	    if (o1 instanceof PiPlugAppHandle) {
		PiPlugAppHandle handle = (PiPlugAppHandle) o1;
		return handle.getApp().getBranding().getName();
	    }
	    return o1.toString();
	}
    }

    private final class CloseShellListener extends MouseAdapter {
	@Override
	public void mouseDown(MouseEvent e) {
	    getShell().setVisible(false);
	    PiPlugRuntimeServices.getInstance().shutdown();
	    getShell().dispose();
	}
    }

    private Composite buttonsArea;
    PiPlugAppContainer container;
    public PiPlugAppHandle runningApp;
    private GridLayout buttonsAreaLayout;
    private GridData buttonsAreaGD;

    public PiPlugDashboardComposite(PiPlugAppContainer container,
	    Map<BundleDescriptor, IPiPlugApplication> applications) {
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
	buttonsAreaLayout = new GridLayout(1, false);
	buttonsAreaLayout.marginWidth = buttonsAreaLayout.marginHeight = 0;
	buttonsAreaLayout.horizontalSpacing = 40;
	buttonsAreaLayout.verticalSpacing = 40;
	buttonsArea.setLayout(buttonsAreaLayout);
	buttonsAreaGD = new GridData(SWT.CENTER, SWT.CENTER, true, true);
	buttonsArea.setLayoutData(buttonsAreaGD);
	updateButtonsLayout(applications);

	for (Entry<BundleDescriptor, IPiPlugApplication> next : applications
		.entrySet())
	    new PiPlugAppHandle(buttonsArea, next.getValue(), theme,
		    next.getKey());
	sortApps();

	Label quit = new Label(this, SWT.NONE);
	GridData gd = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false);
	gd.widthHint = 48;
	gd.heightHint = 48;
	quit.setLayoutData(gd);
	quit.setImage(PiPlugUIActivator.loadImage("images/icon-quit48.png"));
	quit.addMouseListener(new CloseShellListener());
	quit.setBackground(theme.getBackgroundColor());
    }

    private boolean updateButtonsLayout(
	    Map<BundleDescriptor, IPiPlugApplication> applications) {
	int newColumns = Math.min(4, applications.size());
	if (newColumns == buttonsAreaLayout.numColumns)
	    return false;
	buttonsAreaLayout.numColumns = newColumns;
	buttonsAreaGD.widthHint = (newColumns * 256) + ((newColumns - 1) * 40);
	return true;
    }

    private void sortApps() {
	List<Control> children = Arrays.asList(buttonsArea.getChildren());
	if (children.isEmpty())
	    return;

	Control oldFirst = children.get(0);

	Collections.sort(children, new AppHandleComparator());

	Control app = children.get(0);
	app.moveAbove(oldFirst);

	for (int i = 1; i < children.size(); i++) {
	    children.get(i).moveBelow(children.get(i - 1));
	}
    }

    public class PiPlugAppHandle extends Composite implements MouseListener,
	    Runnable {

	public class UnloadExistingBundleRunnable implements Runnable {

	    @Override
	    public void run() {
		if (runningApp == PiPlugAppHandle.this)
		    container.switchToHome();
		try {
		    app.suspend(container.getServices());
		} catch (Throwable t) {
		    t.printStackTrace();
		}
		try {
		    app.shutdown(container.getServices());
		} catch (Throwable t) {
		    t.printStackTrace();
		}
		if (child != null) {
		    child.dispose();
		    child = null;
		}
		app = null;
		PiPlugAppHandle.this.dispose();
		changedAppHandles();
	    }

	}

	private IPiPlugApplication app;
	private Label label;
	private Composite child;
	private BundleDescriptor descriptor;

	public PiPlugAppHandle(Composite parent, IPiPlugApplication app,
		IPiPlugUITheme theme, BundleDescriptor descriptor) {
	    super(parent, SWT.NONE);
	    this.app = app;
	    this.descriptor = descriptor;
	    createControls(theme);
	    app.installed(container.getServices());
	}

	public IPiPlugApplication getApp() {
	    return app;
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

	public void unloadExisting() {
	    getDisplay().syncExec(new UnloadExistingBundleRunnable());
	}

	public void finishUpgrade(BundleDescriptor descriptor) {
	    // app = discoverAppForBundle(descriptor);
	    // app.installed(container.getServices());
	    // // TODO: refresh "Label button" with new image and title
	}

	@Override
	public void mouseUp(MouseEvent arg0) {
	    // nothing to do
	}

	public BundleDescriptor getDescriptor() {
	    return descriptor;
	}
    }

    public void suspendRunningApp() {
	if (runningApp != null)
	    runningApp.suspend();
    }

    public PiPlugAppHandle findAppHandle(BundleDescriptor descriptor) {
	if (descriptor == null)
	    return null;
	FindAppHandleRunnable runnable = new FindAppHandleRunnable(descriptor);
	getDisplay().syncExec(runnable);
	return runnable.getAppHandle();
    }

    public void setApplications(Map<BundleDescriptor, IPiPlugApplication> apps) {
	getDisplay().syncExec(new SetApplicationsRunnable(apps));
    }

    private class SetApplicationsRunnable implements Runnable {

	private Map<BundleDescriptor, IPiPlugApplication> apps;

	public SetApplicationsRunnable(
		Map<BundleDescriptor, IPiPlugApplication> apps) {
	    this.apps = apps;
	}

	public void run() {
	    IPiPlugUITheme theme = container.getTheme();
	    boolean madeChanges = false;
	    for (Entry<BundleDescriptor, IPiPlugApplication> next : apps
		    .entrySet()) {
		BundleDescriptor descriptor = next.getKey();
		if (findAppHandle(descriptor) != null)
		    continue;
		madeChanges = true;
		IPiPlugApplication app = next.getValue();
		new PiPlugAppHandle(buttonsArea, app, theme, descriptor);
		app.installed(container.getServices());
	    }

	    if (updateButtonsLayout(apps) || madeChanges) {
		changedAppHandles();
	    }
	}
    }

    public PiPlugAppHandle findAppHandleID(BundleDescriptor descriptor) {
	if (descriptor == null)
	    return null;
	Control[] appHandles = buttonsArea.getChildren();
	for (Control control : appHandles) {
	    if (control instanceof PiPlugAppHandle) {
		PiPlugAppHandle handle = (PiPlugAppHandle) control;
		if (descriptor.matchesID(handle.getDescriptor()))
		    return handle;
	    }
	}
	return null;
    }

    private void changedAppHandles() {
	sortApps();
	buttonsArea.layout();
	layout();
	buttonsArea.redraw();
    }
}