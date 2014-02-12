package com.genuitec.piplug.poc;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class PiPlugPOCApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Display display = new Display();
		Rectangle bounds = display.getBounds();
		final Shell shell = new Shell(display, SWT.CLOSE);
		shell.setBounds(0, 0, bounds.width, bounds.height);
		shell.setLayout(new GridLayout(1, false));
		final Label label = new Label(shell, SWT.NONE);
		label.setText("Hello to your Pi.");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		Button button = new Button(shell, SWT.FLAT);
		button.setText("Click Me");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				label.setText("We have been clicked.");
				shell.layout(true);
			}
		});
		button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		button = new Button(shell, SWT.FLAT);
		button.setText("Close Me");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
				shell.dispose();
			}
		});
		button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		shell.setMaximized(true);
		shell.open();
		
		// run the display loop
		while (display.sleep() && !shell.isDisposed()) {
			while (display.readAndDispatch() && !shell.isDisposed());
		}
		return new Integer(0);
	}

	@Override
	public void stop() {
		// nothing to do
	}
}