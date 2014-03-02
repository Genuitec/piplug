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

public class FruitController extends Thread {
    GameField gameField = null;
    private Cell[][] cells = null;
    private int x, y = 0;

    private boolean sw = false;
    private boolean isPaused = false;

    public FruitController(GameField param) {
	gameField = param;
	cells = gameField.getCells();
    }

    public void stop(boolean param) {
	sw = param;
    }

    public void run() {
	SnakePlugin
		.trace(this.getClass().getName(), "FruitController started."); //$NON-NLS-1$

	// Set the name of the thread.
	Thread.currentThread().setName("Snake - FruitController"); //$NON-NLS-1$

	while (!sw) {
	    if (!isPaused) {
		if (cells[y][x].getPoints() == 0) {
		    int aux = 0;
		    // Show the fruit.
		    do {
			aux = (int) Math.rint(Math.random()
				* gameField.getRows() * gameField.getColumns());
			y = aux / gameField.getColumns();
			x = aux % gameField.getColumns();
		    } while (aux == gameField.getRows()
			    * gameField.getColumns()
			    || cells[y][x].isSnake()
			    || cells[y][x].getPoints() > 0
			    || cells[y][x].isMine());

		    do {
			aux = (int) Math.rint(Math.random() * 5);
		    } while (aux == 0);
		    cells[y][x].setImage(SnakePlugin.getResourceManager()
			    .getImage("fruit" + aux + ".png")); //$NON-NLS-1$ //$NON-NLS-2$
		    cells[y][x].setModified(true);
		    cells[y][x].setPoints(aux);

		    SnakePlugin
			    .trace(this.getClass().getName(),
				    "Showing a fruit of " + aux + " points at [" + y + "][" + x + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		    Display.getDefault().asyncExec(new Runnable() {
			public void run() {
			    gameField.redraw();
			    // Play sound.
			    if (gameField.getPreferences().isSoundEnabled())
				SnakePlugin.getResourceManager()
					.getSound(Constants.SOUND_APPEAR)
					.play();
			}
		    });
		}
	    }
	    try {
		Thread.sleep(4000);
	    } catch (InterruptedException ex) {
	    }
	}
	SnakePlugin.trace(this.getClass().getName(),
		"FruitController finished."); //$NON-NLS-1$
    }

    public void pauseGame() {
	SnakePlugin.trace(this.getClass().getName(), "FruitController paused."); //$NON-NLS-1$
	isPaused = true;
    }

    public void continueGame() {
	SnakePlugin
		.trace(this.getClass().getName(), "FruitController resumed."); //$NON-NLS-1$
	isPaused = false;
    }
}