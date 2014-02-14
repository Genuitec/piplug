/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import java.util.NoSuchElementException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import russotto.zplet.ZColor;
import russotto.zplet.zmachine.ZMachine;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ZScreen extends Canvas implements KeyListener, PaintListener {
    int lines;
    int chars; /* in fixed font */
    Font fixedfont;
    FontMetrics fixedmetrics;
    Font variablefont;
    Font graphicsfont;
    SyncVector inputcodes;
    Vector bufferedcodes;
    boolean bufferdone;
    ZWindow inputwindow;
    ZCursor inputcursor;
    Image backing_image;
    int zforeground = ZColor.Z_BLACK;
    int zbackground = ZColor.Z_WHITE;
    boolean hasscrolled = false;
    public ZMachine zm;
    public final static String DEFAULT_FONT_FAMILY = "Courier";
    public final static int DEFAULT_FONT_SIZE = 20;
    final static char accent_table[] = { '\u00e4', /* a-umlaut */
    '\u00f6', /* o-umlaut */
    '\u00fc', /* u-umlaut */
    '\u00c4', /* A-umlaut */
    '\u00d6', /* O-umlaut */
    '\u00dc', /* U-umlaut */
    '\u00df', /* sz-ligature */
    '\u00bb', /* right-pointing quote */
    '\u00ab', /* left-pointing quote */
    '\u00eb', /* e-umlaut */
    '\u00ef', /* i-umlaut */
    '\u00ff', /* y-umlaut */
    '\u00cb', /* E-umlaut */
    '\u00cf', /* I-umlaut */
    '\u00e1', /* a-acute */
    '\u00e9', /* e-acute */
    '\u00ed', /* i-acute */
    '\u00f3', /* o-acute */
    '\u00fa', /* u-acute */
    '\u00fd', /* y-acute */
    '\u00c1', /* A-acute */
    '\u00c9', /* E-acute */
    '\u00cd', /* I-acute */
    '\u00d3', /* O-acute */
    '\u00da', /* U-acute */
    '\u00dd', /* Y-acute */
    '\u00e0', /* a-grave */
    '\u00e8', /* e-grave */
    '\u00ec', /* i-grave */
    '\u00f2', /* o-grave */
    '\u00f9', /* u-grave */
    '\u00c0', /* A-grave */
    '\u00c8', /* E-grave */
    '\u00cc', /* I-grave */
    '\u00d2', /* O-grave */
    '\u00d9', /* U-grave */
    '\u00e2', /* a-circumflex */
    '\u00ea', /* e-circumflex */
    '\u00ee', /* i-circumflex */
    '\u00f4', /* o-circumflex */
    '\u00fb', /* u-circumflex */
    '\u00c2', /* A-circumflex */
    '\u00ca', /* E-circumflex */
    '\u00ce', /* I-circumflex */
    '\u00d4', /* O-circumflex */
    '\u00da', /* U-circumflex */
    '\u00e5', /* a-ring */
    '\u00c5', /* A-ring */
    '\u00f8', /* o-slash */
    '\u00d8', /* O-slash */
    '\u00e3', /* a-tilde */
    '\u00f1', /* n-tilde */
    '\u00f5', /* o-tilde */
    '\u00c3', /* A-tilde */
    '\u00d1', /* N-tilde */
    '\u00d5', /* O-tilde */
    '\u00e6', /* ae-ligature */
    '\u00c6', /* AE-ligature */
    '\u00e7', /* c-cedilla */
    '\u00c7', /* C-cedilla */
    '\u00fe', /* Icelandic thorn */
    '\u00f0', /* Icelandic eth */
    '\u00de', /* Icelandic Thorn */
    '\u00d0', /* Icelandic Eth */
    '\u00a3', /* UK pound symbol */
    '\u0153', /* oe ligature */
    '\u0152', /* OE ligature */
    '\u00a1', /* inverse-! */
    '\u00bf', /* inverse-? */
    };

    public ZScreen(Composite parent) {
	this(parent, DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE);
    }

    public ZScreen(Composite parent, String font_family, int font_size) {
	super(parent, SWT.NONE);
	setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	this.setFixedFont(font_family, font_size);
	addPaintListener(this);
	addKeyListener(this);
	setFocus();
    }

    protected boolean isterminator(int key) {
	return ((key == 10) || (key == 13));
    }

    /*
     * public boolean gotFocus(Event evt, Object what) {
     * System.err.println("ZScreen got focus"); return false; }
     * 
     * public boolean lostFocus(Event evt, Object what) {
     * System.err.println("ZScreen lost focus"); return false; }
     */
    static char zascii_to_unicode(short zascii) {
	if ((zascii >= 32) && (zascii <= 126)) /* normal ascii */
	    return (char) zascii;
	else if ((zascii >= 155) && (zascii <= 251)) {
	    if ((zascii - 155) < accent_table.length) {
		return accent_table[zascii - 155];
	    } else
		return '?';
	} else if ((zascii == 0) || (zascii >= 256)) {
	    return '?';
	} else {
	    System.err.println("Illegal character code: " + zascii);
	    return '?';
	}
    }

    static short unicode_to_zascii(char unicode) throws NoSuchKeyException {
	short i;

	if (unicode == '\n')
	    return 13;
	if (unicode == '\b')
	    return 127;
	else if (((int) unicode < 0x20) && (unicode != '\r' /* ' ' */)
		&& (unicode != '\uu001b'))
	    throw new NoSuchKeyException("Illegal character input: "
		    + (short) unicode);
	else if ((int) unicode < 0x80) /* normal ascii, including DELETE */
	    return (short) unicode;
	else {
	    for (i = 0; i < accent_table.length; i++) {
		if (accent_table[i] == unicode)
		    return (short) (155 + i);
	    }
	    throw new NoSuchKeyException("Illegal character input: "
		    + (short) unicode);
	}
    }

    static short fkey_to_zascii(int fkey) throws NoSuchKeyException {
	switch (fkey) {
	case SWT.ARROW_UP:
	    return 129;
	case SWT.ARROW_DOWN:
	    return 130;
	case SWT.ARROW_LEFT:
	    return 131;
	case SWT.ARROW_RIGHT:
	    return 132;
	case SWT.F1:
	    return 133;
	case SWT.F2:
	    return 134;
	case SWT.F3:
	    return 135;
	case SWT.F4:
	    return 136;
	case SWT.F5:
	    return 137;
	case SWT.F6:
	    return 138;
	case SWT.F7:
	    return 139;
	case SWT.F8:
	    return 140;
	case SWT.F9:
	    return 141;
	case SWT.F10:
	    return 142;
	case SWT.F11:
	    return 143;
	case SWT.F12:
	    return 144;
	default:
	    throw new NoSuchKeyException("Illegal function key " + fkey);
	}
    }

    @Override
    public void keyPressed(KeyEvent e) {

	short code;

	/* TODO: e, key to code */
	try {
	    // if (e.keyCode == Event.KEY_PRESS)
	    if (e.character > 0)
		code = unicode_to_zascii((char) e.character);
	    else
		code = fkey_to_zascii(e.keyCode);

	    inputcodes.syncAddElement(new Integer(code));
	} catch (NoSuchKeyException excpt) {
	    // swallow
	}
    }

    public void set_input_window(ZWindow thewindow) {
	inputwindow = thewindow;
    }

    public short read_code() {
	Integer thecode = null;

	while (thecode == null) {
	    thecode = (Integer) inputcodes.syncPopFirstElement(zm);
	    if (!zm.isRunning())
		throw new TerminateZMachineException();
	}
	return (short) thecode.intValue();
    }

    public short read_buffered_code() { /* should really be synched */
	Integer thecode;
	int incode;
	int cw, ch;

	inputwindow.flush();
	cw = fixedmetrics.getAverageCharWidth();
	ch = fixedmetrics.getHeight();

	// inputcursor.setGraphics(getGraphics());
	// inputcursor.setcolors(getForeground(), getBackground());
	inputcursor.setcolors(getForeground(), getBackground());
	inputcursor.size(cw, ch);

	while (!bufferdone) {
	    inputwindow.flush();
	    inputcursor.move(
		    (inputwindow.getLeft() + inputwindow.cursorx) * cw,
		    (inputwindow.getTop() + inputwindow.cursory) * ch);
	    inputcursor.show();
	    incode = read_code();
	    // inputcursor.setGraphics(getGraphics());
	    inputcursor.hide();
	    if ((incode == 8) || (incode == 127)) {
		try {
		    thecode = (Integer) bufferedcodes.lastElement();
		    bufferedcodes.removeElementAt(bufferedcodes.size() - 1);
		    inputwindow.flush();
		    inputwindow.movecursor(inputwindow.cursorx - 1,
			    inputwindow.cursory);
		    inputwindow.printzascii((short) ' ');
		    inputwindow.flush();
		    inputwindow.movecursor(inputwindow.cursorx - 1,
			    inputwindow.cursory);
		} catch (NoSuchElementException booga) {
		    /* ignore */
		}
	    } else {
		if (isterminator(incode)) {
		    bufferdone = true;
		    if ((incode == 10) || (incode == 13))
			inputwindow.newline();
		} else {
		    inputwindow.printzascii((short) incode);
		    inputwindow.flush();
		}
		bufferedcodes.addElement(new Integer(incode));
	    }
	}
	thecode = (Integer) bufferedcodes.firstElement();
	bufferedcodes.removeElementAt(0);
	if (bufferedcodes.isEmpty()) {
	    bufferdone = false;
	}
	return (short) (thecode.intValue());
    }

    public int getlines() {
	return lines;
    }

    public int getchars() {
	return chars;
    }

    public int charwidth() { /* character width of the fixed font */
	return fixedmetrics.getAverageCharWidth();
    }

    /**
     * Set the main font for the game. The Font Family can be any legal font
     * name. However use of a non-fixed width font could cause many unexpected
     * problems. Use at your own risk. Setting font_size to zero or below will
     * set the size to DEFAULT_FONT_SIZE.
     * 
     * @param font_family
     *            a Font Family sting (i.e. "Courier").
     * @param font_size
     *            the point size of the font (int).
     */
    public synchronized void setFixedFont(String font_family, int font_size) {
	if (font_size <= 0) {
	    font_size = DEFAULT_FONT_SIZE;
	}
	this.fixedfont = new Font(getDisplay(), font_family, font_size,
		SWT.NONE);
	setFont(fixedfont);
    }

    /**
     * Get the main font for the game.
     * 
     * @return a java.awt.Font object.
     */
    public synchronized Font getFixedFont() {
	return this.fixedfont;
    }

    public synchronized void settext(int y, int x, char newtext[], int offset,
	    int length) {
	settext(y, x, newtext, offset, length, false, fixedfont);
    }

    public synchronized void settext(int y, int x, char newtext[], int offset,
	    int length, boolean reverse, Font textfont) {

	GC backing_gc = setupGC();
	try {
	    backing_gc.setFont(textfont);
	    drawtext(backing_gc, y, x, newtext, offset, length, reverse);
	} finally {
	    backing_gc.dispose();
	}
	redraw(x, y, getBounds().width, fixedmetrics.getHeight(), false);
    }

    private GC setupGC() {
	GC gc = new GC(backing_image);
	// gc.setAdvanced(true);
	// gc.setTextAntialias(SWT.ON);
	return gc;
    }

    protected synchronized void drawtext(GC backing_gc, int y, int x,
	    char newtext[], int offset, int length, boolean reverse) {
	int tw, th;
	int tx, ty;

	tw = length * charwidth();
	th = fixedmetrics.getHeight();
	tx = x * charwidth();
	ty = th * y;
	if (reverse) {
	    backing_gc.setBackground(getForeground());
	    backing_gc.fillRectangle(tx, th * y, tw, th);
	    backing_gc.setForeground(getBackground());
	} else {
	    backing_gc.setBackground(getBackground());
	    backing_gc.fillRectangle(tx, th * y, tw, th);
	    backing_gc.setForeground(getForeground());
	}

	String toDraw = new String(newtext, offset, length);
	backing_gc.drawString(toDraw, tx, ty);
	backing_gc.setForeground(getForeground());
    }

    public synchronized void scrollLines(int top, int height, int lines) {
	int texttop;

	int boundsWidth = getBounds().width;
	int lineHeight = fixedmetrics.getHeight();
	texttop = top * lineHeight;
	GC backing_gc = setupGC();
	try {
	    backing_gc.copyArea(0, texttop + lines * lineHeight, boundsWidth,
		    (height - lines) * lineHeight, 0, texttop);
	    backing_gc.setBackground(getBackground());
	    backing_gc.fillRectangle(0, texttop + ((height - 1) * lineHeight),
		    boundsWidth, lineHeight);
	} finally {
	    backing_gc.dispose();
	}
	redraw();
	hasscrolled = true;
    }

    public void clear() {
	if (backing_image != null) {
	    GC backing_gc = setupGC();
	    try {
		Rectangle mysize = getBounds();
		backing_gc.setBackground(getBackground());
		backing_gc.fillRectangle(0, 0, mysize.width, mysize.height);
	    } finally {
		backing_gc.dispose();
	    }
	    redraw();
	}
    }

    public int getZForeground() {
	return zforeground;
    }

    public int getZBackground() {
	return zbackground;
    }

    public void setZForeground(int zcolor) {
	zforeground = zcolor;
	setForeground(ZColor.getcolor(zcolor));
    }

    public void setZBackground(int zcolor) {
	zbackground = zcolor;
	setBackground(ZColor.getcolor(zcolor));
    }

    @Override
    public void keyReleased(KeyEvent e) {
	// not used
    }

    @Override
    public void paintControl(PaintEvent e) {

	if (backing_image == null) {
	    // TODO: Figure out when to defer calculating size (after layout)
	    Rectangle mysize = getBounds();

	    inputcodes = new SyncVector();
	    bufferedcodes = new Vector();
	    setForeground(ZColor.getcolor(zforeground));
	    setBackground(ZColor.getcolor(zbackground));

	    backing_image = new Image(getDisplay(), mysize.width, mysize.height);
	    GC backing_gc = setupGC();
	    try {
		backing_gc.setBackground(getBackground());
		backing_gc.fillRectangle(0, 0, mysize.width, mysize.height);

		backing_gc.setFont(fixedfont);
		fixedmetrics = backing_gc.getFontMetrics();
		chars = mysize.width / fixedmetrics.getAverageCharWidth();
		lines = mysize.height / fixedmetrics.getHeight();

		backing_gc.setForeground(getForeground());
	    } finally {
		backing_gc.dispose();
	    }

	    inputcursor = new ZCursor(this, backing_image);
	}

	e.gc.drawImage(backing_image, e.x, e.y, e.width, e.height, e.x, e.y,
		e.width, e.height);
    }

    public boolean isPainted() {
	return fixedmetrics != null && lines > 0 && inputcursor != null;
    }
}
