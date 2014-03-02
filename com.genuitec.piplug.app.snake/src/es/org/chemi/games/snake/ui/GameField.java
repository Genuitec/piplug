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

package es.org.chemi.games.snake.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.core.FruitController;
import es.org.chemi.games.snake.core.PrizeController;
import es.org.chemi.games.snake.core.SnakeController;
import es.org.chemi.games.snake.util.Constants;
import es.org.chemi.games.snake.util.Counter;
import es.org.chemi.games.snake.util.Preferences;

public class GameField extends Composite {
    private static final Color black = Display.getCurrent().getSystemColor(
	    SWT.COLOR_BLACK);
    Cell cells[][] = null;
    int rows = 0;
    int columns = 0;
    Image doubleBuffering = null;

    private FruitController fruits = null;
    private PrizeController prize = null;
    SnakeController snake = null;

    MainView view = null;

    boolean hasCorrectSize = false;
    boolean isGameStopped = true;
    boolean isGamePaused = false;
    boolean isKeyboardLocked = true;

    public GameField(Composite param1, int param2, MainView param3) {
	// Calling parent constructor.
	super(param1, param2);
	view = param3;

	// Adding Control Listener
	this.addControlListener(new ControlAdapter() {
	    public void controlResized(ControlEvent e) {
		int xModule = getSize().x % Constants.CELL_SIZE;
		int yModule = getSize().y % Constants.CELL_SIZE;
		SnakePlugin.trace(this.getClass().getName(),
			"Calculating correct size for the game field."); //$NON-NLS-1$
		if (xModule == getBorderWidth() * 2
			&& yModule == getBorderWidth() * 2) {
		    SnakePlugin.trace(this.getClass().getName(),
			    "Size is correct."); //$NON-NLS-1$
		    hasCorrectSize = true;
		} else {
		    SnakePlugin.trace(this.getClass().getName(),
			    "Setting correct size for the game field."); //$NON-NLS-1$
		    if (xModule > 0) {
			if (xModule < Constants.CELL_SIZE / 2)
			    xModule = -xModule;
			else
			    xModule = Constants.CELL_SIZE - xModule;
		    }
		    if (yModule > 0) {
			if (yModule < Constants.CELL_SIZE / 2)
			    yModule = -yModule;
			else
			    yModule = Constants.CELL_SIZE - yModule;
		    }
		    setSize(getSize().x + xModule + getBorderWidth() * 2,
			    getSize().y + yModule + getBorderWidth() * 2);
		}

		if (hasCorrectSize) {
		    stopGame(false);
		    resetGame();
		    columns = getSize().x / Constants.CELL_SIZE;
		    rows = getSize().y / Constants.CELL_SIZE;
		    createGameField();
		    hasCorrectSize = false;
		}
	    }
	});

	this.addKeyListener(new KeyAdapter() {
	    public void keyPressed(KeyEvent ev) {
		if (!isKeyboardLocked
			&& (ev.keyCode == SWT.ARROW_RIGHT
				|| ev.keyCode == SWT.ARROW_LEFT
				|| ev.keyCode == SWT.ARROW_UP
				|| ev.keyCode == SWT.ARROW_DOWN || Character
				.toLowerCase(ev.character) == 'p')) {
		    if (isGameStopped
			    && Character.toLowerCase(ev.character) != 'p') {
			SnakePlugin
				.trace(this.getClass().getName(),
					"Key "  + ev.keyCode + " pressed while game is stopped. Starting game."); //$NON-NLS-1$ //$NON-NLS-2$
			isGameStopped = false;
			this.setSnakeMovement(ev);
			startGame();
		    } else if (isGamePaused) {
			SnakePlugin
				.trace(this.getClass().getName(),
					"Key "  + ev.keyCode + " pressed while game is paused. Resuming game."); //$NON-NLS-1$ //$NON-NLS-2$
			this.setSnakeMovement(ev);
			continueGame();
			// view.getPauseAction().setIsGamePaused(false);
		    } else if (Character.toLowerCase(ev.character) == 'p') {
			pauseGame();
			// view.getPauseAction().setIsGamePaused(true);
		    } else {
			SnakePlugin
				.trace(this.getClass().getName(),
					"Key "  + ev.keyCode + " pressed while game is running."); //$NON-NLS-1$ //$NON-NLS-2$
			this.setSnakeMovement(ev);
		    }
		} else
		    SnakePlugin
			    .trace(this.getClass().getName(),
				    "Key "  + ev.keyCode + " pressed while keyboard is locked."); //$NON-NLS-1$ //$NON-NLS-2$
	    }

	    public void setSnakeMovement(KeyEvent ev) {
		if (ev.keyCode == SWT.ARROW_RIGHT)
		    snake.setMovement(SWT.RIGHT);
		else if (ev.keyCode == SWT.ARROW_LEFT)
		    snake.setMovement(SWT.LEFT);
		else if (ev.keyCode == SWT.ARROW_UP)
		    snake.setMovement(SWT.UP);
		else if (ev.keyCode == SWT.ARROW_DOWN)
		    snake.setMovement(SWT.DOWN);
	    }
	});

	this.addPaintListener(new PaintListener() {
	    public void paintControl(PaintEvent e) {
		SnakePlugin.trace(this.getClass().getName(),
			"Refresh process of game field started."); //$NON-NLS-1$

		// Double buffering.
		GC tempGC = new GC(doubleBuffering);
		tempGC.setBackground(black);
		tempGC.setForeground(black);

		for (int i = 0; i < rows; i++) {
		    for (int j = 0; j < columns; j++) {
			if (cells[i][j].isModified()) {
			    cells[i][j].paintCell(tempGC);
			    cells[i][j].setModified(false);
			}
		    }
		}
		tempGC.dispose();
		e.gc.drawImage(doubleBuffering, 0, 0);

		SnakePlugin.trace(this.getClass().getName(),
			"Refresh process of game field finished."); //$NON-NLS-1$
	    }
	});
    }

    public void createGameField() {
	// Creating the game field.
	SnakePlugin
		.trace(this.getClass().getName(),
			"Creating the game field with " + rows + " rows and " + columns + " columns."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	cells = new Cell[rows][columns];
	for (int i = 0; i < rows; i++)
	    for (int j = 0; j < columns; j++) {
		Cell cell = new Cell(this, j * Constants.CELL_SIZE, i
			* Constants.CELL_SIZE);
		cells[i][j] = cell;
		cells[i][j].setModified(true);
	    }
	if (doubleBuffering != null)
	    doubleBuffering.dispose();
	doubleBuffering = new Image(null, getSize().x, getSize().y);

	// Creating the snake.
	SnakePlugin.trace(this.getClass().getName(), "Creating the snake."); //$NON-NLS-1$
	int y = 0;
	int x = 0;

	if (rows % 2 != 0)
	    y = rows / 2;
	else
	    y = rows / 2 - 1;

	if (columns % 2 != 0)
	    x = columns / 2;
	else
	    x = columns / 2 - 1;

	// Create Snake.
	snake = new SnakeController(this);
	snake.setHead(cells[y][x + 2]);
	snake.setTail(cells[y][x - 2]);
	snake.setMovement(SWT.NONE);

	// Paint Snake.
	cells[y][x + 2].setImage(SnakePlugin.getResourceManager().getImage(
		Constants.IMAGE_HEAD_RIGHT));
	cells[y][x + 2].setOrientation(SWT.RIGHT);
	cells[y][x + 2].setModified(true);
	cells[y][x + 2].setSnake(true);
	cells[y][x + 1].setImage(SnakePlugin.getResourceManager().getImage(
		Constants.IMAGE_BODY_HORIZONTAL));
	cells[y][x + 1].setOrientation(SWT.RIGHT);
	cells[y][x + 1].setNextCell(cells[y][x + 2]);
	cells[y][x + 1].setModified(true);
	cells[y][x + 1].setSnake(true);
	cells[y][x].setImage(SnakePlugin.getResourceManager().getImage(
		Constants.IMAGE_BODY_HORIZONTAL));
	cells[y][x].setOrientation(SWT.RIGHT);
	cells[y][x].setNextCell(cells[y][x + 1]);
	cells[y][x].setModified(true);
	cells[y][x].setSnake(true);
	cells[y][x - 1].setImage(SnakePlugin.getResourceManager().getImage(
		Constants.IMAGE_BODY_HORIZONTAL));
	cells[y][x - 1].setOrientation(SWT.RIGHT);
	cells[y][x - 1].setNextCell(cells[y][x]);
	cells[y][x - 1].setModified(true);
	cells[y][x - 1].setSnake(true);
	cells[y][x - 2].setImage(SnakePlugin.getResourceManager().getImage(
		Constants.IMAGE_TAIL_RIGHT));
	cells[y][x - 2].setOrientation(SWT.RIGHT);
	cells[y][x - 2].setNextCell(cells[y][x - 1]);
	cells[y][x - 2].setModified(true);
	cells[y][x - 2].setSnake(true);

	// Paint mines on Expert Mode.
	if (getPreferences().getMode().equals(Constants.MODE_EXPERT)) {
	    SnakePlugin.trace(this.getClass().getName(), "Creating the mines."); //$NON-NLS-1$
	    int aux = 0;
	    int cont = rows;

	    while (cont > 0) {
		do {
		    aux = (int) Math.rint(Math.random() * rows * columns);
		    y = aux / columns;
		    x = aux % columns;
		} while (aux == rows * columns || cells[y][x].isSnake()
			|| cells[y][x].getPoints() > 0 || cells[y][x].isMine());

		cells[y][x].setImage(SnakePlugin.getResourceManager().getImage(
			Constants.IMAGE_MINE));
		cells[y][x].setModified(true);
		cells[y][x].setMine(true);
		cont--;
	    }
	}

	isKeyboardLocked = false; // Unlock the keyboard
    }

    public void startGame() {
	SnakePlugin.trace(this.getClass().getName(), "Starting the game."); //$NON-NLS-1$
	fruits = new FruitController(this);
	prize = new PrizeController(this);
	fruits.start();
	prize.start();
	snake.start();
    }

    public void stopGame(boolean isCrashed) {
	SnakePlugin.trace(this.getClass().getName(), "Stopping the game."); //$NON-NLS-1$
	isKeyboardLocked = true; // Lock keyboard
	isGameStopped = true;

	stopThreads();

	// Remove the fruits before the threads finish.
	this.redraw();
    }

    public void resetGame() {
	SnakePlugin.trace(this.getClass().getName(), "Resetting the game."); //$NON-NLS-1$
	view.getCounter().reset();
	isGamePaused = false;
    }

    public void pauseGame() {
	SnakePlugin.trace(this.getClass().getName(), "Pausing the game."); //$NON-NLS-1$
	isGamePaused = true;
	fruits.pauseGame();
	prize.pauseGame();
	snake.pauseGame();
    }

    public boolean isGamePaused() {
	return isGamePaused;
    }

    public void continueGame() {
	SnakePlugin.trace(this.getClass().getName(), "Resumming the game."); //$NON-NLS-1$
	isGamePaused = false;
	fruits.continueGame();
	prize.continueGame();
	snake.continueGame();
    }

    public void stopThreads() {
	if (fruits != null)
	    fruits.stop(true);
	if (prize != null)
	    prize.stop(true);
	if (snake != null)
	    snake.stop(true);
    }

    public Cell[][] getCells() {
	return cells;
    }

    public int getRows() {
	return rows;
    }

    public int getColumns() {
	return columns;
    }

    public Counter getCounter() {
	return view.getCounter();
    }

    public Preferences getPreferences() {
	return view.getPreferences();
    }
}