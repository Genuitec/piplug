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
import org.eclipse.jface.resource.ImageDescriptor;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.nls.SnakeMessages;
import es.org.chemi.games.snake.ui.MainView;

public class SoundAction extends Action {
    private MainView view = null;

    public SoundAction(String label, ImageDescriptor image, MainView view) {
	super(label, image);
	this.view = view;
	this.setToolTipText(SnakeMessages.getString("SoundAction.disable")); //$NON-NLS-1$
	if (this.view.getPreferences().isSoundEnabled())
	    this.setChecked(true);
	else
	    this.setChecked(false);
    }

    public void run() {
	SnakePlugin.trace(this.getClass().getName(), "Change sound mode."); //$NON-NLS-1$

	if (this.view.getPreferences().isSoundEnabled()) {
	    this.view.getPreferences().setSoundEnabled(false);
	    this.setToolTipText(SnakeMessages.getString("SoundAction.enable")); //$NON-NLS-1$
	} else {
	    this.view.getPreferences().setSoundEnabled(true);
	    this.setToolTipText(SnakeMessages.getString("SoundAction.disable")); //$NON-NLS-1$
	}
	view.setFocus(); // Workaround to Bugzilla Bug 42670
    }
}