/************************************************************
 *
 * Copyright (c) 2001 Chemi. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/mit-license.html
 *
 ************************************************************/

package es.org.chemi.games.snake.util;

import java.applet.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import es.org.chemi.games.snake.SnakePlugin;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ResourceManager {
    private HashMap colors = null;
    private HashMap images = null;
    private HashMap fonts = null;
    private HashMap sounds = null;
    private HashMap urls = null;

    private String owner = null;

    public ResourceManager(String owner) {
	SnakePlugin.trace(this.getClass().getName(),
		"Creating a Resource Manager. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	colors = new HashMap();
	images = new HashMap();
	fonts = new HashMap();
	sounds = new HashMap();
	urls = new HashMap();
	this.owner = owner;
    }

    public void putImage(String param1, Image param2) {
	images.put(param1, param2);
    }

    public Image getImage(String param1) {
	return (Image) images.get(param1);
    }

    public void putFont(String param1, Font param2) {
	fonts.put(param1, param2);
    }

    public Font getFont(String param1) {
	return (Font) fonts.get(param1);
    }

    public void putColor(int param1, Color param2) {
	colors.put(new Integer(param1), param2);
    }

    public Color getColor(int param1) {
	return (Color) colors.get(new Integer(param1));
    }

    public void putSound(String param1, AudioClip param2) {
	sounds.put(param1, param2);
    }

    public AudioClip getSound(String param1) {
	return (AudioClip) sounds.get(param1);
    }

    public void putURL(String param1, URL param2) {
	urls.put(param1, param2);
    }

    public URL getURL(String param1) {
	return (URL) urls.get(param1);
    }

    // Free resources
    public void dispose() {
	SnakePlugin.trace(this.getClass().getName(),
		"Disposing colors. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	Iterator i = colors.values().iterator();
	while (i.hasNext())
	    ((Color) i.next()).dispose();

	SnakePlugin.trace(this.getClass().getName(),
		"Disposing fonts. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	i = fonts.values().iterator();
	while (i.hasNext())
	    ((Font) i.next()).dispose();

	SnakePlugin.trace(this.getClass().getName(),
		"Disposing images. (" + owner + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	i = images.values().iterator();
	while (i.hasNext())
	    ((Image) i.next()).dispose();
    }
}
