package com.genuitec.piplug.app.clock;

import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ClockFace extends Canvas implements PaintListener, Runnable {

    private static final double TWO_PI = 2.0 * Math.PI;
    private Color black, white, red;
    private int _diameter;
    private int _centerX;
    private int _centerY;
    private Calendar _now;

    public ClockFace(Composite parent) {
	super(parent, SWT.DOUBLE_BUFFERED);
	addPaintListener(this);
	black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	_now = Calendar.getInstance();
    }

    @Override
    public void paintControl(PaintEvent e) {

	// prepare the clock face
	e.gc.setBackground(black);
	e.gc.setForeground(white);
	e.gc.setAdvanced(true);
	e.gc.setAntialias(SWT.ON);
	Rectangle client = getClientArea();
	e.gc.fillRectangle(client);
	int size = Math.min(client.width, client.height) - 60;
	Rectangle clock = new Rectangle(0, 0, size, size);
	clock.x = Math.max(0, client.width - size) / 2;
	clock.y = Math.max(0, client.height - size) / 2;

	_diameter = size;
	_centerX = clock.x + (clock.width / 2);
	_centerY = clock.y + (clock.height / 2);

	drawClockFace(e.gc);
	drawClockHands(e.gc);
	e.gc.setBackground(white);
	int circle = (int) (size * 0.02);
	e.gc.fillOval(_centerX - circle, _centerY - circle, circle * 2,
		circle * 2);
    }

    private void drawClockHands(GC gc) {
	// ... Get the various time elements from the Calendar object.
	int hours = _now.get(Calendar.HOUR);
	int minutes = _now.get(Calendar.MINUTE);
	int seconds = _now.get(Calendar.SECOND);
	int millis = _now.get(Calendar.MILLISECOND);

	// ... second hand
	int handMax = (int) (_diameter * 0.44);
	double fseconds = (seconds + (double) millis / 1000) / 60.0;
	gc.setForeground(red);
	gc.setLineWidth(1);
	drawRadius(gc, fseconds, 0, handMax);

	// ... minute hand
	handMax = _diameter / 3;
	double fminutes = (minutes + fseconds) / 60.0;
	gc.setForeground(white);
	gc.setLineWidth(2);
	drawRadius(gc, fminutes, 0, handMax);

	// ... hour hand
	handMax = _diameter / 4;
	gc.setForeground(white);
	gc.setLineWidth(4);
	drawRadius(gc, (hours + fminutes) / 12.0, 0, handMax);
    }

    private void drawClockFace(GC gc) {
	int radius = _diameter / 2;

	// ... Draw the tick marks around the circumference.
	for (int sec = 0; sec < 60; sec++) {
	    int tickStart;
	    if (sec % 5 == 0) {
		gc.setLineWidth(6);
		tickStart = (int) (radius * 0.83); // Draw long tick every 5.
	    } else {
		gc.setLineWidth(4);
		tickStart = (int) (radius * 0.94); // Short tick mark.
	    }
	    drawRadius(gc, sec / 60.0, tickStart, radius);
	}
    }

    private void drawRadius(GC gc, double percent, int minRadius, int maxRadius) {
	// ... percent parameter is the fraction (0.0 - 1.0) of the way
	// clockwise from 12. Because the Graphics2D methods use radians
	// counterclockwise from 3, a little conversion is necessary.
	// It took a little experimentation to get this right.
	double radians = (0.5 - percent) * TWO_PI;
	double sine = Math.sin(radians);
	double cosine = Math.cos(radians);

	int dxmin = _centerX + (int) (minRadius * sine);
	int dymin = _centerY + (int) (minRadius * cosine);

	int dxmax = _centerX + (int) (maxRadius * sine);
	int dymax = _centerY + (int) (maxRadius * cosine);
	gc.drawLine(dxmin, dymin, dxmax, dymax);
    }

    public void updateTime() {
	_now.setTimeInMillis(System.currentTimeMillis());
	getDisplay().syncExec(this);
    }

    public void run() {
	redraw();
    }
}
