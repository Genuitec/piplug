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

package es.org.chemi.games.snake.util;

public class Preferences {
    private String mode = null;
    private boolean soundEnabled = true;
    private boolean transparentEnabled = false;

    // Setters.
    public void setMode(String param) {
	this.mode = param;
    }

    public void setSoundEnabled(boolean param) {
	this.soundEnabled = param;
    }

    public void setTransparentEnabled(boolean param) {
	this.transparentEnabled = param;
    }

    // Getters
    public String getMode() {
	return this.mode;
    }

    public boolean isSoundEnabled() {
	return soundEnabled;
    }

    public boolean isTransparentEnable() {
	return transparentEnabled;
    }
}