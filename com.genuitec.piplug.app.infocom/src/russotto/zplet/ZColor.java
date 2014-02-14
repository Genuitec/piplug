/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ZColor {
	public final static int Z_CURRENT = 0;
	public final static int Z_DEFAULT = 1;
	public final static int Z_BLACK = 2;
	public final static int Z_RED = 3;
	public final static int Z_GREEN = 4;
	public final static int Z_YELLOW = 5;
	public final static int Z_BLUE = 6;
	public final static int Z_MAGENTA = 7;
	public final static int Z_CYAN = 8;
	public final static int Z_WHITE = 9;

	private static Color black, red, green, yellow, blue, magenta, cyan, white, gray;
	
	public static Color getcolor(int number) {
		switch (number) {
		case Z_BLACK:
			if (black == null)
				black = new Color(Display.getDefault(), 0, 0, 0);
			return black;
		case Z_RED:
			if (red == null)
				red = new Color(Display.getDefault(), 255, 0, 0);
			return red;
		case Z_GREEN:
			if (green == null)
				green = new Color(Display.getDefault(), 0, 255, 0);
			return green;
		case Z_YELLOW:
			if (yellow == null)
				yellow = new Color(Display.getDefault(), 255, 255, 0);
			return yellow;
		case Z_BLUE:
			if (blue == null)
				blue = new Color(Display.getDefault(), 0, 0, 255);
			return blue;
		case Z_MAGENTA:
			if (magenta == null)
				magenta = new Color(Display.getDefault(), 255, 0, 255);
			return magenta;
		case Z_CYAN:
			if (cyan == null)
				cyan = new Color(Display.getDefault(), 0, 255, 255);
			return cyan;
		case Z_WHITE:
			if (white == null)
				white = new Color(Display.getDefault(), 255, 255, 255);
			return white;
		}
		if (gray == null)
			gray = new Color(Display.getDefault(), 128, 128, 128);
		return gray;
	}

	public static Color getcolor(String name) {
		if (name.equalsIgnoreCase("black"))
			return getcolor(Z_BLACK);
		if (name.equalsIgnoreCase("red"))
			return getcolor(Z_RED);
		if (name.equalsIgnoreCase("green"))
			return getcolor(Z_GREEN);
		if (name.equalsIgnoreCase("yellow"))
			return getcolor(Z_YELLOW);
		if (name.equalsIgnoreCase("blue"))
			return getcolor(Z_BLUE);
		if (name.equalsIgnoreCase("magenta"))
			return getcolor(Z_MAGENTA);
		if (name.equalsIgnoreCase("cyan"))
			return getcolor(Z_CYAN);
		if (name.equalsIgnoreCase("white"))
			return getcolor(Z_WHITE);
		return getcolor(Z_DEFAULT); // gray
	}

	public static int getcolornumber(String name) {
		if (name.equalsIgnoreCase("black"))
			return Z_BLACK;
		if (name.equalsIgnoreCase("red"))
			return Z_RED;
		if (name.equalsIgnoreCase("green"))
			return Z_GREEN;
		if (name.equalsIgnoreCase("yellow"))
			return Z_YELLOW;
		if (name.equalsIgnoreCase("blue"))
			return Z_BLUE;
		if (name.equalsIgnoreCase("magenta"))
			return Z_MAGENTA;
		if (name.equalsIgnoreCase("cyan"))
			return Z_CYAN;
		if (name.equalsIgnoreCase("white"))
			return Z_WHITE;
		return Z_BLACK;
	}
}

