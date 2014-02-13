package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugUITheme implements IPiPlugUITheme {

    private Color backgroundColor;
    private Font titleFont, subtitleFont;
    private Color titleColor, subtitleColor;

    public PiPlugUITheme(Shell shell) {
	backgroundColor = new Color(Display.getDefault(), 255, 255, 255);
	FontData fontBase = shell.getFont().getFontData()[0];
	titleFont = new Font(Display.getDefault(), fontBase.getName(),
		fontBase.getHeight() + 4, SWT.NONE);
	titleColor = new Color(Display.getDefault(), 35, 31, 35);
	subtitleFont = new Font(Display.getDefault(), fontBase.getName(),
		fontBase.getHeight() + 2, SWT.NONE);
	subtitleColor = new Color(Display.getDefault(), 88, 89, 91);
    }

    @Override
    public Image[] getShellImages() {
	Image[] images = new Image[5];
	images[0] = PiPlugUIActivator.loadImage("images/PiPlug-Icon-256.png");
	images[1] = PiPlugUIActivator.loadImage("images/PiPlug-Icon-128.png");
	images[2] = PiPlugUIActivator.loadImage("images/PiPlug-Icon-64.png");
	images[3] = PiPlugUIActivator.loadImage("images/PiPlug-Icon-32.png");
	images[4] = PiPlugUIActivator.loadImage("images/PiPlug-Icon-16.png");
	return images;
    }

    @Override
    public Image getHeaderLogoImage() {
	return PiPlugUIActivator.loadImage("images/PiPlug-wtext-50h.png");
    }

    @Override
    public Color getBackgroundColor() {
	return backgroundColor;
    }

    @Override
    public Color getTitleColor() {
	return titleColor;
    }

    @Override
    public Color getSubtitleColor() {
	return subtitleColor;
    }

    @Override
    public Font getTitleFont() {
	return titleFont;
    }

    @Override
    public Font getSubtitleFont() {
	return subtitleFont;
    }
}