/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ZStatus extends Composite {
    boolean timegame;
    boolean initialized;
    boolean chronograph;
    String location;
    int score;
    int turns;
    int hours;
    int minutes;
    Label Right;
    Label Center;
    Label Left;

    public ZStatus(Composite parent) {
	super(parent, SWT.NONE);
	GridLayout layout = new GridLayout(3, false);
	layout.marginWidth = layout.marginHeight = 0;
	layout.horizontalSpacing = 0;
	setLayout(layout);
	Left = new Label(this, SWT.LEFT);
	GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
	gd.widthHint = 200;
	Left.setLayoutData(gd);
	Center = new Label(this, SWT.CENTER);
	Center.setText("use save/restore/quit");
	Center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	Right = new Label(this, SWT.RIGHT);
	gd = new GridData(SWT.FILL, SWT.FILL, true, false);
	gd.widthHint = 200;
	Right.setLayoutData(gd);
	chronograph = false;
    }

    @Override
    public void setFont(Font font) {
	Left.setFont(font);
	Center.setFont(font);
	Right.setFont(font);
    }

    @Override
    public void setBackground(Color color) {
	Left.setBackground(color);
	Center.setBackground(color);
	Right.setBackground(color);
    }

    @Override
    public void setForeground(Color color) {
	Left.setForeground(color);
	Center.setForeground(color);
	Right.setForeground(color);
    }

    /*
     * public boolean gotFocus(Event evt, Object what) {
     * System.err.println("ZStatus got focus"); return false; }
     * 
     * public boolean lostFocus(Event evt, Object what) {
     * System.err.println("ZStatus lost focus"); return false; }
     */

    public void update_score_line(String location, int score, int turns) {
	this.timegame = false;
	this.location = location;
	this.score = score;
	this.turns = turns;
	Left.setText(location);
	Right.setText(score + "/" + turns);
    }

    public void update_time_line(String location, int hours, int minutes) {
	String meridiem;

	this.timegame = true;
	this.location = location;
	this.hours = hours;
	this.minutes = minutes;
	Left.setText(location);
	if (chronograph) {
	    Right.setText(hours + ":" + minutes);
	} else {
	    if (hours < 12)
		meridiem = "AM";
	    else
		meridiem = "PM";
	    hours %= 12;
	    if (hours == 0)
		hours = 12;
	    Right.setText(hours + ":" + minutes + meridiem);
	}
    }

    public void clear() {
	Left.setText("");
	Right.setText("");
    }
}
