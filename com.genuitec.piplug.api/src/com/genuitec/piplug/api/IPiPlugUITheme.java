package com.genuitec.piplug.api;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

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
}