/************************************************************
 *
 * Copyright (c) 2003 Chemi. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/mit-license.html
 *
 ************************************************************/

package es.org.chemi.games.snake;

import java.util.Calendar;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import es.org.chemi.games.snake.util.Constants;
import es.org.chemi.games.snake.util.ResourceManager;

/**
 * The main plugin class to be used in the desktop.
 */
public class SnakePlugin extends Plugin {
    // The shared instance.
    private static SnakePlugin plugin;
    // Resource bundle.
    private ResourceBundle resourceBundle;
    // Resource manager.
    private static ResourceManager resourceManager = null;

    public SnakePlugin() {
	super();
	plugin = this;
	resourceManager = new ResourceManager(Constants.PLUGIN_ID);
    }

    public static SnakePlugin getDefault() {
	return plugin;
    }

    public static String getResourceString(String key) {
	ResourceBundle bundle = SnakePlugin.getDefault().getResourceBundle();
	try {
	    return bundle.getString(key);
	} catch (MissingResourceException e) {
	    return key;
	}
    }

    public ResourceBundle getResourceBundle() {
	return resourceBundle;
    }

    public static ResourceManager getResourceManager() {
	return resourceManager;
    }

    public static void trace(String className, String message) {
	if (SnakePlugin.getDefault().isDebugging())
	    if (Platform
		    .getDebugOption("es.org.chemi.games.snake/debug/filter").equals("*") || className.indexOf(Platform.getDebugOption("es.org.chemi.games.snake/debug/filter").substring(0, Platform.getDebugOption("es.org.chemi.games.snake/debug/filter").length() - 1)) != -1) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		System.out
			.println("[Snake - " + className + "] " + Calendar.getInstance().getTime() + " - " + message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void start(BundleContext context) throws Exception {
	super.start(context);

	SnakePlugin
		.trace(this.getClass().getName(),
			"Loading " + this.getBundle().getSymbolicName() + " plug-in (version: " + this.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	try {
	    resourceBundle = ResourceBundle
		    .getBundle("es.org.chemi.games.snake.SnakePluginResources"); //$NON-NLS-1$
	} catch (MissingResourceException x) {
	    resourceBundle = null;
	}

    }

    public void stop(BundleContext context) throws Exception {
	super.stop(context);
	SnakePlugin
		.trace(this.getClass().getName(),
			"Unloading " + this.getBundle().getSymbolicName() + " plug-in (version: " + this.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}