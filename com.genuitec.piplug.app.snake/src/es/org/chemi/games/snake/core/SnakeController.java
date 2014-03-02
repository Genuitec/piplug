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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.ui.Cell;
import es.org.chemi.games.snake.ui.GameField;
import es.org.chemi.games.snake.util.Constants;

public class SnakeController extends Thread {
    GameField gameField = null;
    private Cell head = null;
    private Cell tail = null;
    private int movement = SWT.NONE;
    private int grow = 0;
    private int sleepTime = 0;

    private boolean sw = false;
    private boolean isPaused = false;
    private boolean isMine = false;
    private boolean isCrashed = false;

    public SnakeController(GameField param) {
	gameField = param;
    }

    public void run() {
	SnakePlugin
		.trace(this.getClass().getName(), "SnakeController started."); //$NON-NLS-1$

	// Set the name of the thread.
	Thread.currentThread().setName("Snake - SnakeController"); //$NON-NLS-1$

	if (gameField.getPreferences().getMode().equals(Constants.MODE_EXPERT))
	    sleepTime = Constants.SPEED_MODE_EXPERT;
	else if (gameField.getPreferences().getMode()
		.equals(Constants.MODE_INTERMEDIATE))
	    sleepTime = Constants.SPEED_MODE_INTERMEDIATE;
	else if (gameField.getPreferences().getMode()
		.equals(Constants.MODE_BEGINNER))
	    sleepTime = Constants.SPEED_MODE_BEGINNER;

	while (!sw) {
	    if (!isPaused) {
		// Play sound.
		if (gameField.getPreferences().isSoundEnabled())
		    SnakePlugin.getResourceManager()
			    .getSound(Constants.SOUND_BIP).play();

		// Head movement.
		if (movement == SWT.NONE) {
		    if (head.getOrientation() == SWT.RIGHT)
			moveHead(Constants.IMAGE_BODY_HORIZONTAL,
				Constants.IMAGE_HEAD_RIGHT, SWT.RIGHT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE + 1,
				Constants.IMAGE_CRASH_RIGHT);
		    else if (head.getOrientation() == SWT.LEFT)
			moveHead(Constants.IMAGE_BODY_HORIZONTAL,
				Constants.IMAGE_HEAD_LEFT, SWT.LEFT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE - 1,
				Constants.IMAGE_CRASH_LEFT);
		    else if (head.getOrientation() == SWT.UP)
			moveHead(Constants.IMAGE_BODY_VERTICAL,
				Constants.IMAGE_HEAD_UP, SWT.UP, head.getY()
					/ Constants.CELL_SIZE - 1, head.getX()
					/ Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_UP);
		    else if (head.getOrientation() == SWT.DOWN)
			moveHead(Constants.IMAGE_BODY_VERTICAL,
				Constants.IMAGE_HEAD_DOWN, SWT.DOWN,
				head.getY() / Constants.CELL_SIZE + 1,
				head.getX() / Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_DOWN);
		} else if (movement == SWT.RIGHT) {
		    if (head.getOrientation() == SWT.RIGHT)
			moveHead(Constants.IMAGE_BODY_HORIZONTAL,
				Constants.IMAGE_HEAD_RIGHT, SWT.RIGHT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE + 1,
				Constants.IMAGE_CRASH_RIGHT);
		    else if (head.getOrientation() == SWT.LEFT)
			moveHead(Constants.IMAGE_BODY_HORIZONTAL,
				Constants.IMAGE_HEAD_LEFT, SWT.LEFT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE - 1,
				Constants.IMAGE_CRASH_LEFT);
		    else if (head.getOrientation() == SWT.UP)
			moveHead(Constants.IMAGE_BODY_DOWNRIGHT,
				Constants.IMAGE_HEAD_RIGHT, SWT.RIGHT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE + 1,
				Constants.IMAGE_CRASH_UP);
		    else if (head.getOrientation() == SWT.DOWN)
			moveHead(Constants.IMAGE_BODY_UPRIGHT,
				Constants.IMAGE_HEAD_RIGHT, SWT.RIGHT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE + 1,
				Constants.IMAGE_CRASH_DOWN);
		} else if (movement == SWT.LEFT) {
		    if (head.getOrientation() == SWT.RIGHT)
			moveHead(Constants.IMAGE_BODY_HORIZONTAL,
				Constants.IMAGE_HEAD_RIGHT, SWT.RIGHT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE + 1,
				Constants.IMAGE_CRASH_RIGHT);
		    else if (head.getOrientation() == SWT.LEFT)
			moveHead(Constants.IMAGE_BODY_HORIZONTAL,
				Constants.IMAGE_HEAD_LEFT, SWT.LEFT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE - 1,
				Constants.IMAGE_CRASH_LEFT);
		    else if (head.getOrientation() == SWT.UP)
			moveHead(Constants.IMAGE_BODY_DOWNLEFT,
				Constants.IMAGE_HEAD_LEFT, SWT.LEFT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE - 1,
				Constants.IMAGE_CRASH_UP);
		    else if (head.getOrientation() == SWT.DOWN)
			moveHead(Constants.IMAGE_BODY_UPLEFT,
				Constants.IMAGE_HEAD_LEFT, SWT.LEFT,
				head.getY() / Constants.CELL_SIZE, head.getX()
					/ Constants.CELL_SIZE - 1,
				Constants.IMAGE_CRASH_DOWN);
		} else if (movement == SWT.UP) {
		    if (head.getOrientation() == SWT.RIGHT)
			moveHead(Constants.IMAGE_BODY_UPLEFT,
				Constants.IMAGE_HEAD_UP, SWT.UP, head.getY()
					/ Constants.CELL_SIZE - 1, head.getX()
					/ Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_RIGHT);
		    else if (head.getOrientation() == SWT.LEFT)
			moveHead(Constants.IMAGE_BODY_UPRIGHT,
				Constants.IMAGE_HEAD_UP, SWT.UP, head.getY()
					/ Constants.CELL_SIZE - 1, head.getX()
					/ Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_LEFT);
		    else if (head.getOrientation() == SWT.UP)
			moveHead(Constants.IMAGE_BODY_VERTICAL,
				Constants.IMAGE_HEAD_UP, SWT.UP, head.getY()
					/ Constants.CELL_SIZE - 1, head.getX()
					/ Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_UP);
		    else if (head.getOrientation() == SWT.DOWN)
			moveHead(Constants.IMAGE_BODY_VERTICAL,
				Constants.IMAGE_HEAD_DOWN, SWT.DOWN,
				head.getY() / Constants.CELL_SIZE + 1,
				head.getX() / Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_DOWN);
		} else if (movement == SWT.DOWN) {
		    if (head.getOrientation() == SWT.RIGHT)
			moveHead(Constants.IMAGE_BODY_DOWNLEFT,
				Constants.IMAGE_HEAD_DOWN, SWT.DOWN,
				head.getY() / Constants.CELL_SIZE + 1,
				head.getX() / Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_RIGHT);
		    else if (head.getOrientation() == SWT.LEFT)
			moveHead(Constants.IMAGE_BODY_DOWNRIGHT,
				Constants.IMAGE_HEAD_DOWN, SWT.DOWN,
				head.getY() / Constants.CELL_SIZE + 1,
				head.getX() / Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_LEFT);
		    else if (head.getOrientation() == SWT.UP)
			moveHead(Constants.IMAGE_BODY_VERTICAL,
				Constants.IMAGE_HEAD_UP, SWT.UP, head.getY()
					/ Constants.CELL_SIZE - 1, head.getX()
					/ Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_UP);
		    else if (head.getOrientation() == SWT.DOWN)
			moveHead(Constants.IMAGE_BODY_VERTICAL,
				Constants.IMAGE_HEAD_DOWN, SWT.DOWN,
				head.getY() / Constants.CELL_SIZE + 1,
				head.getX() / Constants.CELL_SIZE,
				Constants.IMAGE_CRASH_DOWN);
		}

		// Tail movement.
		if (!isCrashed) {
		    if (grow > 0) {
			SnakePlugin.trace(this.getClass().getName(),
				"Growing snake."); //$NON-NLS-1$
			grow--;
		    } else {
			SnakePlugin
				.trace(this.getClass().getName(),
					"Move tail to [" + tail.getY() + "][" + tail.getX() + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tail.setImage(null);
			tail.setModified(true);
			tail.setSnake(false);
			Cell aux = tail.getNextCell();
			tail.setNextCell(null);
			tail = aux;
			if (tail.getOrientation() == SWT.RIGHT)
			    moveTail(Constants.IMAGE_TAIL_RIGHT);
			else if (tail.getOrientation() == SWT.LEFT)
			    moveTail(Constants.IMAGE_TAIL_LEFT);
			else if (tail.getOrientation() == SWT.UP)
			    moveTail(Constants.IMAGE_TAIL_UP);
			else if (tail.getOrientation() == SWT.DOWN)
			    moveTail(Constants.IMAGE_TAIL_DOWN);
		    }
		}

		// Reset movement
		movement = SWT.NONE;

		// Repaint
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
			try {
			    gameField.redraw();
			} catch (SWTException ex) {
			    // Theoretically fired when closing the game while
			    // the timer still working.
			}
		    }
		});
	    }
	    // Sleep
	    try {
		Thread.sleep(sleepTime);
	    } catch (InterruptedException ex) {
	    }
	}

	SnakePlugin.trace(this.getClass().getName(),
		"SnakeController finished."); //$NON-NLS-1$
    }

    public void stop(boolean param) {
	sw = param;
    }

    public void setHead(Cell param) {
	head = param;
    }

    public void setTail(Cell param) {
	tail = param;
    }

    public void setMovement(int param) {
	movement = param;
    }

    private void moveTail(String tailImage) {
	tail.setImage(SnakePlugin.getResourceManager().getImage(tailImage));
	tail.setModified(true);
    }

    private void moveHead(String bodyImage, String headImage, int orientation,
	    int y, int x, String crashImage) {
	SnakePlugin.trace(this.getClass().getName(),
		"Move head to [" + y + "][" + x + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (gameField.getPreferences().isTransparentEnable()
		&& (x == -1 || x == gameField.getColumns() || y == -1 || y == gameField
			.getRows())) {
	    if (x == -1) {
		x = gameField.getColumns() - 1;
		// Play sound.
		if (gameField.getPreferences().isSoundEnabled())
		    SnakePlugin.getResourceManager()
			    .getSound(Constants.SOUND_ACROSS).play();
	    } else if (x == gameField.getColumns()) {
		x = 0;
		// Play sound.
		if (gameField.getPreferences().isSoundEnabled())
		    SnakePlugin.getResourceManager()
			    .getSound(Constants.SOUND_ACROSS).play();
	    } else if (y == -1) {
		y = gameField.getRows() - 1;
		// Play sound.
		if (gameField.getPreferences().isSoundEnabled())
		    SnakePlugin.getResourceManager()
			    .getSound(Constants.SOUND_ACROSS).play();
	    } else if (y == gameField.getRows()) {
		y = 0;
		// Play sound.
		if (gameField.getPreferences().isSoundEnabled())
		    SnakePlugin.getResourceManager()
			    .getSound(Constants.SOUND_ACROSS).play();
	    }
	}

	SnakePlugin.trace(this.getClass().getName(),
		"Checking if the movement is ok to [" + y + "][" + x + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	if (isMovementOk(y, x)) {
	    if (isFruit(y, x)) {
		final int points = gameField.getCells()[y][x].getPoints();
		SnakePlugin.trace(this.getClass().getName(),
			"A prize or a fruit of " + points + " has been eaten."); //$NON-NLS-1$ //$NON-NLS-2$
		// Play sound.
		if (gameField.getPreferences().isSoundEnabled())
		    SnakePlugin.getResourceManager()
			    .getSound(Constants.SOUND_FRUIT).play();
		if (points != 10)
		    grow = points;
		gameField.getCells()[y][x].setPoints(0);
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
			gameField.getCounter().increase(points);
		    }
		});
	    }
	    Cell aux = null;
	    head.setImage(SnakePlugin.getResourceManager().getImage(bodyImage));
	    head.setOrientation(orientation);
	    head.setModified(true);
	    aux = head;

	    head = gameField.getCells()[y][x];
	    aux.setNextCell(head);
	    head.setImage(SnakePlugin.getResourceManager().getImage(headImage));
	    head.setOrientation(orientation);
	    head.setModified(true);
	    head.setSnake(true);
	} else
	    stopGame(crashImage);
    }

    private boolean isMovementOk(int y, int x) {
	if (x == -1 || x == gameField.getColumns() || y == -1
		|| y == gameField.getRows()
		|| gameField.getCells()[y][x].isSnake())
	    return false;
	else if (gameField.getCells()[y][x].isMine()) {
	    isMine = true;
	    return false;
	} else
	    return true;
    }

    private boolean isFruit(int y, int x) {
	if (gameField.getCells()[y][x].getPoints() == 0)
	    return false;
	else
	    return true;
    }

    private void stopGame(String param) {
	SnakePlugin.trace(this.getClass().getName(), "Snake has crashed."); //$NON-NLS-1$

	isCrashed = true;

	// Play sound.
	if (gameField.getPreferences().isSoundEnabled()) {
	    if (isMine) {
		SnakePlugin.getResourceManager()
			.getSound(Constants.SOUND_EXPLODE2).play();
		isMine = false;
	    } else
		SnakePlugin.getResourceManager()
			.getSound(Constants.SOUND_EXPLODE).play();
	}
	head.setImage(SnakePlugin.getResourceManager().getImage(param));
	head.setModified(true);

	Display.getDefault().asyncExec(new Runnable() {
	    public void run() {
		gameField.stopGame(true);
	    }
	});
    }

    public void pauseGame() {
	SnakePlugin.trace(this.getClass().getName(), "SnakeController paused."); //$NON-NLS-1$
	isPaused = true;
    }

    public void continueGame() {
	SnakePlugin
		.trace(this.getClass().getName(), "SnakeController resumed."); //$NON-NLS-1$
	isPaused = false;
    }
}