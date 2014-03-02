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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.util.Constants;

public class Cell {
    protected Composite gameField = null;
    private Image image = null;

    private int x = 0;
    private int y = 0;

    private Cell nextCell = null;
    private int orientation = SWT.NONE;
    private boolean isModified = false;
    private boolean isSnake = false;
    private int points = 0;
    private boolean isMine = false;

    public Cell(Composite gameField, int x, int y) {
	SnakePlugin.trace(this.getClass().getName(),
		"Creating new cell [" + x + "][" + y + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	this.gameField = gameField;
	this.x = x;
	this.y = y;
    }

    public void paintCell(GC gc) {
	SnakePlugin.trace(this.getClass().getName(),
		"Refreshing cell [" + x + "][" + y + "]."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	if (image != null)
	    gc.drawImage(image, this.x, this.y);
	else
	    gc.fillRectangle(this.x, this.y, Constants.CELL_SIZE,
		    Constants.CELL_SIZE);
    }

    /**
     * @param image
     */
    public void setImage(Image image) {
	this.image = image;
    }

    public Image getImage() {
	return this.image;
    }

    public void setNextCell(Cell param) {
	nextCell = param;
    }

    public int getOrientation() {
	return orientation;
    }

    public void setOrientation(int param) {
	orientation = param;
    }

    public Cell getNextCell() {
	return nextCell;
    }

    public void setModified(boolean isModified) {
	this.isModified = isModified;
    }

    public boolean isModified() {
	return isModified;
    }

    public void setSnake(boolean isSnake) {
	this.isSnake = isSnake;
    }

    public boolean isSnake() {
	return isSnake;
    }

    public void setMine(boolean isMine) {
	this.isMine = isMine;
    }

    public boolean isMine() {
	return isMine;
    }

    public void setPoints(int points) {
	this.points = points;
    }

    public int getPoints() {
	return points;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }
}