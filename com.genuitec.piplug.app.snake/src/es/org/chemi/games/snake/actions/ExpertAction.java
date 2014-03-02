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

public class ExpertAction extends Action {
    private MainView view = null;

    public ExpertAction(String label, MainView view) {
	super(label);
	this.view = view;
    }

    public void run() {
	SnakePlugin.trace(this.getClass().getName(),
		"Changing game mode to expert."); //$NON-NLS-1$

	view.updateActionsUI(Constants.MODE_EXPERT);
	view.getPreferences().setMode(Constants.MODE_EXPERT);

	// Restart the game.
	if (view.getPreferences().isSoundEnabled())
	    SnakePlugin.getResourceManager().getSound(Constants.SOUND_START)
		    .play();
	view.getGameField().stopGame(false);
	view.getGameField().resetGame();
	view.getGameField().createGameField();
	// view.getPauseAction().setIsGamePaused(false);
	view.setFocus();
    }
}
