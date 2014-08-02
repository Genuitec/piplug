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
	Display.setAppName("PiPlug");
	Display display = new Display();
	Rectangle bounds = display.getBounds();
	final Shell shell = new Shell(display, SWT.NO_TRIM);
	int width = bounds.width;
	int height = bounds.height;
	String[] arguments = (String[]) context.getArguments().get(
		"application.args");
	for (int i = 0; i < arguments.length; i++) {
	    String string = arguments[i];
	    if (string.equals("-width")) {
		width = Integer.valueOf(arguments[i + 1]);
	    }
	    if (string.equals("-height")) {
		height = Integer.valueOf(arguments[i + 1]);
	    }
	}
	shell.setBounds(0, 0, width, height);
	shell.setText("PiPlug: Plug in Apps to your Pi");
	IPiPlugUITheme theme = null;
	if (shell.getSize().x == 320) {
	    theme = new QVGAPiPlugUITheme(shell);
	} else {
	    theme = new PiPlugUITheme(shell);
	}
	PiPlugServices services = new PiPlugServices(theme);
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = layout.marginHeight = 0;
	shell.setLayout(layout);
	shell.setImages(theme.getShellImages());
	shell.setBackground(theme.getBackgroundColor());
	PiPlugAppContainer container = new PiPlugAppContainer(shell, services);
	PiPlugStartingUpComposite startup = new PiPlugStartingUpComposite(
		container, theme);
	container.activate(startup);
	shell.setMaximized(true);
	shell.open();

	PiPlugRuntimeServices.getInstance().startup(container, startup);

	// run the display loop
	while (!shell.isDisposed() && display.sleep()) {
	    try {
		while (!shell.isDisposed() && display.readAndDispatch()) {
		    // process events
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return new Integer(0);
    }

    @Override
    public void stop() {
	// nothing needed in stop
    }
}