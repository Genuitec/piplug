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

package es.org.chemi.games.snake.core;

import org.eclipse.swt.widgets.Display;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.ui.Cell;
import es.org.chemi.games.snake.ui.GameField;
import es.org.chemi.games.snake.util.Constants;

public class PrizeController extends Thread {
    GameField gameField = null;
    private Cell[][] cells = null;

    private boolean sw = false;
    private boolean isGamePaused = false;

    public PrizeController(GameField param) {
	gameField = param;
	cells = gameField.getCells();
    }

    public void stop(boolean param) {
	sw = param;
    }

    public void run() {
	SnakePlugin
		.trace(this.getClass().getName(), "PrizeController started."); //$NON-NLS-1$

	// Set the name of the thread.
	Thread.currentThread().setName("Snake - PrizeController"); //$NON-NLS-1$

	while (!sw) {
	    try {
		Thread.sleep(20000);
	    } catch (InterruptedException ex) {
	    }

	    if (!isGamePaused && !sw) {
		// Show the fruit.
		int y = 0;
		int x = 0;
		int aux = 0;

		do {
		    aux = (int) Math.rint(Math.random() * gameField.getRows()
			    * gameField.getColumns());
		    y = aux / gameField.getColumns();
		    x = aux % gameField.getColumns();
		} while (aux == gameField.getRows() * gameField.getColumns()
			|| cells[y][x].isSnake() || cells[y][x].getPoints() > 0
			|| cells[y][x].isMine());

		cells[y][x].setImage(SnakePlugin.getResourceManager().getImage(
			Constants.IMAGE_PRIZE));
		cells[y][x].setModified(true);
		cells[y][x].setPoints(10);

		SnakePlugin
			.trace(this.getClass().getName(),
				"Showing a prize of 10 points at [" + y + "][" + x + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
			gameField.redraw();
			// Play sound.
			if (gameField.getPreferences().isSoundEnabled())
			    SnakePlugin.getResourceManager()
				    .getSound(Constants.SOUND_APPEAR).play();
		    }
		});

		// Hide the fruit.
		try {
		    Thread.sleep(7000);
		} catch (InterruptedException ex) {
		}
		if (!sw) {
		    if (cells[y][x].getPoints() != 0) {
			SnakePlugin
				.trace(this.getClass().getName(),
					"Hidding a prize of 10 points at [" + y + "][" + x + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cells[y][x].setImage(null);
			cells[y][x].setModified(true);
			cells[y][x].setPoints(0);
			Display.getDefault().asyncExec(new Runnable() {
			    public void run() {
				gameField.redraw();
			    }
			});
		    }
		}
	    }
	}
	SnakePlugin.trace(this.getClass().getName(),
		"PrizeController finished."); //$NON-NLS-1$
    }

    public void pauseGame() {
	SnakePlugin.trace(this.getClass().getName(), "PrizeController paused."); //$NON-NLS-1$
	isGamePaused = true;
    }

    public void continueGame() {
	SnakePlugin
		.trace(this.getClass().getName(), "PrizeController resumed."); //$NON-NLS-1$
	isGamePaused = false;
    }
}