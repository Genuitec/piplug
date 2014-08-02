package com.genuitec.piplug.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.genuitec.piplug.api.IPiPlugUITheme;

/**
 * A PiPlug theme for supporting quarter size VGA screens (320x240 pixels).
 * 
 * @author Torkild U. Resheim
 */
public class QVGAPiPlugUITheme extends PiPlugUITheme implements IPiPlugUITheme {

    public QVGAPiPlugUITheme(Shell shell) {
	super(shell);
	appIconSize = new Point(98, 48);
	FontData fontBase = shell.getFont().getFontData()[0];
	titleFont = new Font(Display.getDefault(), fontBase.getName(),
		fontBase.getHeight() + 2, SWT.NONE);
	subtitleFont = new Font(Display.getDefault(), fontBase.getName(),
		fontBase.getHeight() + 1, SWT.NONE);
    }

    @Override
    public Image getHeaderLogoImage() {
	return PiPlugUIActivator.loadImage("images/PiPlug-wText-25h.png");
    }

    @Override
    public int getMargin() {
	return 8;
    }

    @Override
    public int getMaximumColumns() {
	return 3;
    }

    @Override
    public Image getQuitIconImage() {
	return PiPlugUIActivator.loadImage("images/icon-quit48.png");
    }

    @Override
    public int getSpacing() {
	return 8;
    }

}