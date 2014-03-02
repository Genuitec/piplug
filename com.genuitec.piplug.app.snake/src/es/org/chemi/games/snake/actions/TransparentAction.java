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

package es.org.chemi.games.snake.actions;

import org.eclipse.jface.action.Action;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.ui.MainView;
import es.org.chemi.games.snake.util.Constants;

public class TransparentAction extends Action {
    private MainView view = null;

    public TransparentAction(String label, MainView view) {
	super(label);
	this.view = view;
    }

    public void run() {
	SnakePlugin.trace(this.getClass().getName(),
		"Change transparent walls mode."); //$NON-NLS-1$

	if (this.view.getPreferences().isTransparentEnable())
	    this.view.getPreferences().setTransparentEnabled(false);
	else
	    this.view.getPreferences().setTransparentEnabled(true);
	view.updateActionsUI(Constants.TRANSPARENT_MODE);
	view.setFocus(); // Workaround to Bugzilla Bug 42670
    }
}
