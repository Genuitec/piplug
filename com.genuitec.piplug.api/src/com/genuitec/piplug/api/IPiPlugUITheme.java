package com.genuitec.piplug.api;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public interface IPiPlugUITheme {

    /**
     * Returns the default images needed for the shell.
     * 
     * @return the shell images.
     */
    public Image[] getShellImages();

    /**
     * Returns the default logo to use for the header bar.
     * 
     * @return the cached logo for the header bar.
     */
    public Image getHeaderLogoImage();

    /**
     * Returns the primary background color.
     * 
     * @return the primary background.
     */
    public Color getBackgroundColor();

    /**
     * Returns the title color.
     * 
     * @return the color.
     */
    public Color getTitleColor();

    /**
     * Returns the subtitle color.
     * 
     * @return the color.
     */
    public Color getSubtitleColor();

    /**
     * Returns the title font.
     * 
     * @return the font.
     */
    public Font getTitleFont();

    /**
     * Returns the subtitle font.
     * 
     * @return the font.
     */
    public Font getSubtitleFont();

    /**
     * Returns a layout margin to use in the application selection section of
     * the dashboard.
     * 
     * @return the margin width and height
     */
    public int getMargin();

    /**
     * Returns a layout spacing to use in the application selection section of
     * the dashboard.
     * 
     * @return the spacing width and height
     */
    public int getSpacing();

    /**
     * Returns the maximum number of columns to use for the application
     * selection section of the dashboard.
     * 
     * @return the maximum number of columns
     */
    public int getMaximumColumns();

    /**
     * Returns the image to use in the quit button displayed in the dashboard.
     * 
     * @return the quit button image
     */
    public Image getQuitIconImage();

    /**
     * Returns the size to use for application icons in the dashboard.
     * 
     * @return the application icon size
     */
    public Point getAppIconSize();

}