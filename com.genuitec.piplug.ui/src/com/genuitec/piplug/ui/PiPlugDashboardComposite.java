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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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
		    if (descriptor.matchesID(handle.getDescriptor())) {
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

	// Create the logo section
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = theme.getMargin();
	layout.marginHeight = theme.getMargin();
	setLayout(layout);
	setBackground(theme.getBackgroundColor());
	Label label = new Label(this, SWT.CENTER);
	label.setImage(theme.getHeaderLogoImage());
	label.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
	label.setBackground(theme.getBackgroundColor());

	// Create the buttons section
	buttonsArea = new Composite(this, SWT.NONE);
	buttonsArea.setBackground(theme.getBackgroundColor());
	buttonsAreaLayout = new GridLayout(1, false);
	buttonsAreaLayout.marginWidth = buttonsAreaLayout.marginHeight = 0;
	buttonsAreaLayout.horizontalSpacing = theme.getSpacing();
	buttonsAreaLayout.verticalSpacing = theme.getSpacing();
	buttonsArea.setLayout(buttonsAreaLayout);
	buttonsAreaGD = new GridData(SWT.CENTER, SWT.CENTER, true, true);
	buttonsArea.setLayoutData(buttonsAreaGD);
	updateButtonsLayout(applications);

	for (Entry<BundleDescriptor, IPiPlugApplication> next : applications
		.entrySet())
	    new PiPlugAppHandle(buttonsArea, next.getValue(), theme,
		    next.getKey());
	sortApps();

	// Create the buttons section
	Label quit = new Label(this, SWT.NONE);
	GridData gd = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false);
	gd.widthHint = theme.getMargin();
	gd.heightHint = theme.getMargin();
	quit.setLayoutData(gd);
	quit.setImage(theme.getQuitIconImage());
	quit.addMouseListener(new CloseShellListener());
	quit.setBackground(theme.getBackgroundColor());
    }

    private boolean updateButtonsLayout(
	    Map<BundleDescriptor, IPiPlugApplication> applications) {
	IPiPlugUITheme theme = container.getTheme();
	int maxColumns = theme.getMaximumColumns();
	int newColumns = Math.min(maxColumns, applications.size());
	if (newColumns == buttonsAreaLayout.numColumns)
	    return false;
	buttonsAreaLayout.numColumns = newColumns;
	buttonsAreaGD.widthHint = (newColumns * theme.getAppIconSize().x)
		+ ((newColumns - 1) * theme.getSpacing());
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

	public class UpdateAppVersionRunnable implements Runnable {
	    @Override
	    public void run() {
		updateAppBranding();
		if (wasRunning) {
		    PiPlugAppHandle.this.run();
		    wasRunning = false;
		}
	    }
	}

	public boolean wasRunning;

	public class UnloadExistingBundleRunnable implements Runnable {

	    private boolean dispose;

	    public UnloadExistingBundleRunnable(boolean dispose) {
		this.dispose = dispose;
	    }

	    @Override
	    public void run() {
		if (runningApp == PiPlugAppHandle.this) {
		    PiPlugAppHandle.this.wasRunning = true;
		    container.switchToHome();
		}
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
		if (dispose)
		    PiPlugAppHandle.this.dispose();
	    }

	}

	private IPiPlugApplication app;
	private Label label;
	private Composite child;
	private BundleDescriptor descriptor;
	private Label button;
	private Image icon;

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
	    layout.verticalSpacing = theme.getSpacing();
	    setLayout(layout);
	    button = new Label(this, SWT.PUSH);
	    button.setBackground(theme.getBackgroundColor());
	    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	    gd.heightHint = theme.getAppIconSize().x;
	    gd.widthHint = theme.getAppIconSize().y;
	    button.setLayoutData(gd);
	    button.addMouseListener(this);
	    label = new Label(this, SWT.CENTER);
	    label.setFont(theme.getTitleFont());
	    label.setForeground(theme.getTitleColor());
	    label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
		    false));
	    label.addMouseListener(this);
	    label.setBackground(theme.getBackgroundColor());
	    addMouseListener(this);
	    gd = new GridData(SWT.FILL, SWT.FILL);
	    gd.widthHint = theme.getAppIconSize().x;
	    gd.heightHint = theme.getAppIconSize().y + 30;
	    setLayoutData(gd);
	    this.setBackground(theme.getBackgroundColor());
	    ImageData original = app.getBranding().getImage().getImageData();
	    ImageData scaled = original.scaledTo(theme.getAppIconSize().x,
		    theme.getAppIconSize().y);
	    icon = new Image(container.getDisplay(), scaled);
	    updateAppBranding();
	}

	private void updateAppBranding() {
	    button.setImage(icon);
	    label.setText(app.getBranding().getName());
	}

	@Override
	public void mouseDoubleClick(MouseEvent arg0) {
	    // nothing to do
	}

	@Override
	public void mouseDown(MouseEvent arg0) {
	    getDisplay().asyncExec(this);
	}

	public void run() {
	    label.setText("Loading...");
	    if (child == null)
		child = app.prepare(container.getServices(), container);
	    container.activate(child);
	    label.setText(app.getBranding().getName());
	    PiPlugDashboardComposite.this.runningApp = this;
	    app.resume(container.getServices());
	}

	public void suspend() {
	    if (app != null)
		app.suspend(container.getServices());
	}

	public void unloadExisting(boolean dispose) {
	    getDisplay().syncExec(new UnloadExistingBundleRunnable(dispose));
	}

	@Override
	public void mouseUp(MouseEvent arg0) {
	    // nothing to do
	}

	public BundleDescriptor getDescriptor() {
	    return descriptor;
	}

	public boolean updateAppIfNeeded(IPiPlugApplication newApp,
		BundleDescriptor newDescriptor) {
	    if (app != null)
		return false;
	    app = newApp;
	    descriptor = newDescriptor;
	    getDisplay().syncExec(new UpdateAppVersionRunnable());
	    return true;
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

    public void setApplications(Map<BundleDescriptor, IPiPlugApplication> apps,
	    boolean removedApps) {
	getDisplay().syncExec(new SetApplicationsRunnable(apps, removedApps));
    }

    private class SetApplicationsRunnable implements Runnable {

	private Map<BundleDescriptor, IPiPlugApplication> apps;
	private boolean removedApps;

	public SetApplicationsRunnable(
		Map<BundleDescriptor, IPiPlugApplication> apps,
		boolean removedApps) {
	    this.apps = apps;
	    this.removedApps = removedApps;
	}

	public void run() {
	    IPiPlugUITheme theme = container.getTheme();
	    boolean madeChanges = false;
	    for (Entry<BundleDescriptor, IPiPlugApplication> next : apps
		    .entrySet()) {
		BundleDescriptor descriptor = next.getKey();
		PiPlugAppHandle appHandle = findAppHandle(descriptor);
		IPiPlugApplication app = next.getValue();
		if (appHandle != null) {
		    madeChanges |= appHandle.updateAppIfNeeded(app, descriptor);
		    continue;
		}
		madeChanges = true;
		new PiPlugAppHandle(buttonsArea, app, theme, descriptor);
		app.installed(container.getServices());
	    }

	    if (updateButtonsLayout(apps) || removedApps || madeChanges) {
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