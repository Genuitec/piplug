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

public class PauseAction extends Action {
    private MainView view = null;
    private boolean isGamePaused = false;

    public PauseAction(String label, ImageDescriptor image, MainView view) {
	super(label, image);
	this.view = view;
	this.setToolTipText(SnakeMessages.getString("PauseAction.pause")); //$NON-NLS-1$

	this.setChecked(false);
	this.isGamePaused = false;
    }

    public void run() {
	SnakePlugin.trace(this.getClass().getName(), "Pause game solicited."); //$NON-NLS-1$

	if (isGamePaused) {
	    setToolTipText(SnakeMessages.getString("PauseAction.pause")); //$NON-NLS-1$
	    isGamePaused = false;
	    view.getGameField().continueGame();
	} else {
	    setToolTipText(SnakeMessages.getString("PauseAction.continue")); //$NON-NLS-1$
	    isGamePaused = true;
	    view.getGameField().pauseGame();
	}
	view.setFocus(); // Workaround to Bugzilla Bug 42670
    }

    public void setIsGamePaused(boolean param) {
	isGamePaused = param;
	setChecked(param);
	if (param == false)
	    setToolTipText(SnakeMessages.getString("PauseAction.pause")); //$NON-NLS-1$
	else
	    setToolTipText(SnakeMessages.getString("PauseAction.continue")); //$NON-NLS-1$
    }
}