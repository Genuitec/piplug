package com.genuitec.piplug.app.clock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.genuitec.piplug.api.IPiPlugUITheme;

public class ClockComposite extends Composite {

	private Label label;
	private DateFormat format;

	public ClockComposite(IPiPlugUITheme theme, Composite parent) {
		super(parent, SWT.NONE);
		setBackground(new Color(getDisplay(), 255, 255, 255));
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		setLayout(layout);
		label = new Label(this, SWT.CENTER);
		label.setFont(theme.getTitleFont());
		format = new SimpleDateFormat("H:mm:ss");
		updateTime();
	}

	public void updateTime() {
		label.setText(format.format(new Date()));
	}
}