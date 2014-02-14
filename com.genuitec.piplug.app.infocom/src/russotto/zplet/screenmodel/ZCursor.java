/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

import russotto.zplet.ZColor;

class ZCursor {
    Color cursorcolor, bgcolor;
    boolean shown;
    int t, l, w, h;
    Canvas parent;
    private Image backing_image;

    ZCursor(Color cursorcolor, Color bgcolor, Canvas parent, Image backing_image) {
	shown = false;
	this.cursorcolor = cursorcolor;
	this.bgcolor = bgcolor;
	this.parent = parent;
	this.backing_image = backing_image;
    }

    ZCursor(Canvas parent, Image backing_image) {
	this(ZColor.getcolor(ZColor.Z_GREEN), ZColor.getcolor(ZColor.Z_YELLOW),
		parent, backing_image);
    }

    synchronized void show() {
	if (!shown) {
	    shown = true;
	    if (parent != null) {
		GC backing_gc = new GC(backing_image);
		try {
		    backing_gc.setBackground(cursorcolor);
		    backing_gc.fillRectangle(l, t, w, h);
		} finally {
		    backing_gc.dispose();
		}
		parent.redraw(l, t, w, h, false);
	    }
	}
    }

    synchronized void hide() {
	if (shown) {
	    shown = false;
	    if (parent != null) {
		GC backing_gc = new GC(backing_image);
		try {
		    backing_gc.setBackground(bgcolor);
		    backing_gc.fillRectangle(l, t, w, h);
		} finally {
		    backing_gc.dispose();
		}
		parent.redraw(l, t, w, h, false);
	    }
	}
    }

    synchronized void move(int l, int t) {
	boolean wasshown = shown;

	if (wasshown)
	    hide();
	this.l = l;
	this.t = t;
	if (wasshown)
	    show();
    }

    synchronized void size(int w, int h) {
	boolean wasshown = shown;

	if (wasshown)
	    hide();
	this.w = w;
	this.h = h;
	if (wasshown)
	    show();
    }

    synchronized void setcolors(Color cursorcolor, Color bgcolor) {
	boolean wasshown = shown;

	if (wasshown)
	    hide();
	this.cursorcolor = cursorcolor;
	this.bgcolor = bgcolor;
	if (wasshown)
	    show();
    }
}
