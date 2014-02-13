package com.genuitec.piplug.ui;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugApplication implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {
	Display display = new Display();
	Rectangle bounds = display.getBounds();
	final Shell shell = new Shell(display, SWT.NO_TRIM);
	IPiPlugUITheme theme = new PiPlugUITheme(shell);
	shell.setBounds(0, 0, bounds.width, bounds.height);
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = layout.marginHeight = 0;
	shell.setLayout(layout);
	shell.setImages(theme.getShellImages());
	shell.setBackground(theme.getBackgroundColor());
	PiPlugAppContainer container = new PiPlugAppContainer(shell, theme);
	PiPlugStartingUpComposite startup = new PiPlugStartingUpComposite(
		container, theme);
	container.activate(startup);
	shell.setMaximized(true);
	shell.open();

	PiPlugStartupJob job = new PiPlugStartupJob(container, startup);
	// idle processor to allow VNC or other display actions to run smoothly
	job.schedule(500);

	// run the display loop
	while (!shell.isDisposed() && display.sleep()) {
	    while (!shell.isDisposed() && display.readAndDispatch()) {
		// process events
	    }
	}
	return new Integer(0);
    }

    @Override
    public void stop() {
	// nothing needed in stop
    }
}