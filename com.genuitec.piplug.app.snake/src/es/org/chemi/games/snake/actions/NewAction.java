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

public class NewAction extends Action {
    private MainView view = null;

    public NewAction(String label, MainView view) {
	super(label);
	this.view = view;
    }

    public void run() {
	SnakePlugin.trace(this.getClass().getName(),
		"New game creation solicited."); //$NON-NLS-1$

	// Restart the game.
	view.getGameField().stopGame(false);
	view.getGameField().resetGame();
	view.getGameField().createGameField();
	// view.getPauseAction().setIsGamePaused(false);
	view.setFocus();
    }
}
