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

import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;

import com.genuitec.piplug.api.IPiPlugServices;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.util.Constants;
import es.org.chemi.games.snake.util.Counter;
import es.org.chemi.games.snake.util.Preferences;

public class MainView {
    public class ResetGameListener extends MouseAdapter {
	@Override
	public void mouseDown(MouseEvent arg0) {
	    SnakePlugin.trace(this.getClass().getName(),
		    "New game creation solicited."); //$NON-NLS-1$

	    SnakePlugin.getResourceManager().getSound(Constants.SOUND_START)
		    .play();

	    // Restart the game.
	    gameField.stopGame(false);
	    gameField.resetGame();
	    gameField.createGameField();
	    // pauseAction.setIsGamePaused(false);
	    setFocus();
	}
    }

    GameField gameField = null;
    private Label mainButton = null;
    private Counter counter = null;

    // private ExpertAction expertAction = null;
    // private IntermediateAction intermediateAction = null;
    // private BeginnerAction beginnerAction = null;
    // private TransparentAction transparentAction = null;
    // PauseAction pauseAction = null;
    private Preferences preferences = null;
    private IPiPlugServices services;

    public class GoHomeListener extends MouseAdapter {
	@Override
	public void mouseDown(MouseEvent e) {
	    services.switchToHome();
	}
    }

    public MainView(IPiPlugServices services) {
	preferences = new Preferences();
	preferences.setMode(Constants.MODE_BEGINNER);
	this.services = services;

	// Loading images.
	loadImages();

	// Loading colors.
	loadColors();

	// Loading sounds.
	loadSounds();
    }

    private void loadImages() {
	// Loading images
	SnakePlugin.trace(this.getClass().getName(), "Loading images."); //$NON-NLS-1$

	ClassLoader classLoader = this.getClass().getClassLoader();
	loadImageWithTransparentColor(Constants.IMAGE_SNAKE);
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_HEAD_UP,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_HEAD_UP))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_HEAD_DOWN,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_HEAD_DOWN))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_HEAD_RIGHT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_HEAD_RIGHT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_HEAD_LEFT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_HEAD_LEFT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_TAIL_UP,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_TAIL_UP))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_TAIL_DOWN,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_TAIL_DOWN))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_TAIL_RIGHT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_TAIL_RIGHT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_TAIL_LEFT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_TAIL_LEFT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_BODY_HORIZONTAL,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_BODY_HORIZONTAL))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_BODY_VERTICAL,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_BODY_VERTICAL))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_BODY_DOWNLEFT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_BODY_DOWNLEFT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_BODY_DOWNRIGHT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_BODY_DOWNRIGHT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_BODY_UPLEFT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_BODY_UPLEFT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_BODY_UPRIGHT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_BODY_UPRIGHT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_CRASH_UP,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_CRASH_UP))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_CRASH_DOWN,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_CRASH_DOWN))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_CRASH_RIGHT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_CRASH_RIGHT))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_CRASH_LEFT,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_CRASH_LEFT))); //$NON-NLS-1$

	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_FRUIT1,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_FRUIT1))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_FRUIT2,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_FRUIT2))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_FRUIT3,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_FRUIT3))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_FRUIT4,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_FRUIT4))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_FRUIT5,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_FRUIT5))); //$NON-NLS-1$
	SnakePlugin
		.getResourceManager()
		.putImage(
			Constants.IMAGE_PRIZE,
			new Image(
				Display.getCurrent(),
				classLoader
					.getResourceAsStream("icons/" + Constants.IMAGE_PRIZE))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putImage(
		Constants.IMAGE_MINE,
		new Image(Display.getCurrent(), classLoader
			.getResourceAsStream("icons/" + Constants.IMAGE_MINE))); //$NON-NLS-1$			

	try {
	    // Load ImageDescriptors.
	    URL url = new URL(SnakePlugin.getDefault().getBundle()
		    .getEntry("/"), "icons/"); //$NON-NLS-1$
	    SnakePlugin.getResourceManager().putURL(Constants.IMAGE_MARK,
		    new URL(url, Constants.IMAGE_MARK));
	    SnakePlugin.getResourceManager().putURL(Constants.IMAGE_PAUSE,
		    new URL(url, Constants.IMAGE_PAUSE));
	    SnakePlugin.getResourceManager().putURL(Constants.IMAGE_SOUND,
		    new URL(url, Constants.IMAGE_SOUND));
	} catch (MalformedURLException ex) {
	    SnakePlugin
		    .getDefault()
		    .getLog()
		    .log(new Status(IStatus.ERROR, SnakePlugin.getDefault()
			    .getBundle().getSymbolicName(), IStatus.ERROR, ex
			    .toString(), null));
	}
    }

    private void loadImageWithTransparentColor(String s) {
	ImageData imageData = new ImageData(this.getClass().getClassLoader()
		.getResourceAsStream("icons/" + s)); //$NON-NLS-1$
	ImageData aux = null;
	if (imageData.transparentPixel > 0)
	    aux = imageData.getTransparencyMask();
	SnakePlugin.getResourceManager().putImage(s,
		new Image(Display.getCurrent(), imageData, aux));
    }

    private void loadColors() {
	// Loading colors.
	SnakePlugin.trace(this.getClass().getName(), "Loading colors."); //$NON-NLS-1$
    }

    private void loadSounds() {
	// Loading sounds
	SnakePlugin.trace(this.getClass().getName(), "Loading sounds."); //$NON-NLS-1$

	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_EXPLODE,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_EXPLODE))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_EXPLODE2,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_EXPLODE2))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_START,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_START))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_BIP,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_BIP))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_FRUIT,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_FRUIT))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_APPEAR,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_APPEAR))); //$NON-NLS-1$
	SnakePlugin.getResourceManager().putSound(
		Constants.SOUND_ACROSS,
		Applet.newAudioClip(this.getClass().getClassLoader()
			.getResource("sounds/" + Constants.SOUND_ACROSS))); //$NON-NLS-1$
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public Composite createComposite(Composite parent) {
	SnakePlugin.trace(this.getClass().getName(),
		"Creation of Snake main view started."); //$NON-NLS-1$

	Composite body = new Composite(parent, SWT.NONE);
	body.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

	// Create UI.
	open(body);

	SnakePlugin.trace(this.getClass().getName(),
		"Creation of Snake main view finished."); //$NON-NLS-1$

	return body;
    }

    private void open(Composite parent) {
	// Setting Composite Layout.
	SnakePlugin.trace(this.getClass().getName(), "Setting layout."); //$NON-NLS-1$
	GridLayout gridLayout = new GridLayout();
	gridLayout.numColumns = 3;
	gridLayout.marginWidth = 48;
	gridLayout.marginHeight = 48;
	gridLayout.horizontalSpacing = 0;
	gridLayout.verticalSpacing = 10;
	gridLayout.makeColumnsEqualWidth = true;
	parent.setLayout(gridLayout);

	// Creating Actions.
	createActions();

	// Creating Panels.
	createView(parent);
    }

    private void createView(Composite parent) {
	// Create the counter.
	SnakePlugin.trace(this.getClass().getName(), "Adding the counter."); //$NON-NLS-1$

	// Create the main button.
	SnakePlugin.trace(this.getClass().getName(), "Adding the main button."); //$NON-NLS-1$
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.BEGINNING;
	gridData.verticalAlignment = GridData.CENTER;
	gridData.widthHint = 48;
	gridData.heightHint = 48;
	mainButton = new Label(parent, SWT.NONE);
	mainButton.setLayoutData(gridData);
	Bundle bundle = Platform.getBundle("com.genuitec.piplug.app.snake");
	URL url = bundle.getEntry("icons/icon-restart48.png");
	mainButton.setImage(ImageDescriptor.createFromURL(url).createImage());
	mainButton.addMouseListener(new ResetGameListener());
	mainButton.setBackground(parent.getBackground());

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.CENTER;
	gridData.verticalAlignment = GridData.CENTER;
	this.counter = new Counter(parent, SWT.BORDER, 4, 0,
		Constants.PLUGIN_ID);
	this.counter.setLayoutData(gridData);

	Label button = new Label(parent, SWT.NONE);
	gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
	gridData.widthHint = 48;
	gridData.heightHint = 48;
	button.setLayoutData(gridData);
	url = bundle.getEntry("icons/icon-close48.png");
	button.setImage(ImageDescriptor.createFromURL(url).createImage());
	button.addMouseListener(new GoHomeListener());
	button.setBackground(parent.getBackground());

	// Create the game field.
	SnakePlugin.trace(this.getClass().getName(), "Adding the game field."); //$NON-NLS-1$
	gridData = new GridData();
	gridData.horizontalSpan = 3;
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;

	// SWT.NO_BACKGROUND is used for Double Buffering.
	gameField = new GameField(parent, SWT.BORDER | SWT.NO_BACKGROUND, this);
	gameField.setLayoutData(gridData);
    }

    private void createActions() {
	// Creating the Menu
	SnakePlugin
		.trace(this.getClass().getName(), "Adding the actions menu."); //$NON-NLS-1$
	/*
	 * IMenuManager menuMgr = this.getViewSite().getActionBars()
	 * .getMenuManager(); menuMgr.add(new
	 * NewAction(SnakeMessages.getString("MainView.new"), this));
	 * //$NON-NLS-1$ menuMgr.add(new Separator());
	 * menuMgr.add(beginnerAction = new BeginnerAction(SnakeMessages
	 * .getString("MainView.beginner"), this)); //$NON-NLS-1$
	 * menuMgr.add(intermediateAction = new IntermediateAction(SnakeMessages
	 * .getString("MainView.intermediate"), this)); //$NON-NLS-1$
	 * menuMgr.add(expertAction = new ExpertAction(SnakeMessages
	 * .getString("MainView.expert"), this)); //$NON-NLS-1$ menuMgr.add(new
	 * Separator()); menuMgr.add(transparentAction = new
	 * TransparentAction(SnakeMessages .getString("MainView.transparent"),
	 * this)); //$NON-NLS-1$
	 * 
	 * URL aux =
	 * SnakePlugin.getResourceManager().getURL(Constants.IMAGE_MARK);
	 * 
	 * if (preferences.getMode().equals(Constants.MODE_EXPERT))
	 * expertAction.setImageDescriptor(ImageDescriptor.createFromURL(aux));
	 * else if (preferences.getMode().equals(Constants.MODE_INTERMEDIATE))
	 * intermediateAction.setImageDescriptor(ImageDescriptor
	 * .createFromURL(aux)); else if
	 * (preferences.getMode().equals(Constants.MODE_BEGINNER))
	 * beginnerAction.setImageDescriptor(ImageDescriptor
	 * .createFromURL(aux));
	 * 
	 * if (preferences.isTransparentEnable())
	 * transparentAction.setImageDescriptor(ImageDescriptor
	 * .createFromURL(aux));
	 * 
	 * // Creating the Buttons SnakePlugin.trace(this.getClass().getName(),
	 * "Adding the action tool bar buttons."); //$NON-NLS-1$ IToolBarManager
	 * toolbarMgr = this.getViewSite().getActionBars() .getToolBarManager();
	 * 
	 * aux = SnakePlugin.getResourceManager().getURL(Constants.IMAGE_SOUND);
	 * toolbarMgr .add(new SoundAction(
	 * SnakeMessages.getString("MainView.sound"),
	 * ImageDescriptor.createFromURL(aux), this)); //$NON-NLS-1$
	 * 
	 * aux = SnakePlugin.getResourceManager().getURL(Constants.IMAGE_PAUSE);
	 * pauseAction = new PauseAction(
	 * SnakeMessages.getString("MainView.pause"),
	 * ImageDescriptor.createFromURL(aux), this); //$NON-NLS-1$
	 * toolbarMgr.add(pauseAction);
	 */
    }

    public void updateActionsUI(String mode) {
	/*
	 * SnakePlugin.trace(this.getClass().getName(),
	 * "Updating action menu marks."); //$NON-NLS-1$ URL aux =
	 * SnakePlugin.getResourceManager().getURL(Constants.IMAGE_MARK);
	 * 
	 * if (mode.equals(Constants.MODE_EXPERT)) {
	 * expertAction.setImageDescriptor(ImageDescriptor.createFromURL(aux));
	 * intermediateAction.setImageDescriptor(null);
	 * beginnerAction.setImageDescriptor(null); } else if
	 * (mode.equals(Constants.MODE_INTERMEDIATE)) {
	 * expertAction.setImageDescriptor(null);
	 * intermediateAction.setImageDescriptor(ImageDescriptor
	 * .createFromURL(aux)); beginnerAction.setImageDescriptor(null); } else
	 * if (mode.equals(Constants.MODE_BEGINNER)) {
	 * expertAction.setImageDescriptor(null);
	 * intermediateAction.setImageDescriptor(null);
	 * beginnerAction.setImageDescriptor(ImageDescriptor
	 * .createFromURL(aux)); }
	 * 
	 * if (mode.equals(Constants.TRANSPARENT_MODE)) { if
	 * (preferences.isTransparentEnable())
	 * transparentAction.setImageDescriptor(ImageDescriptor
	 * .createFromURL(aux)); else
	 * transparentAction.setImageDescriptor(null); }
	 */
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
	gameField.setFocus();
    }

    public Counter getCounter() {
	return counter;
    }

    public Preferences getPreferences() {
	return preferences;
    }

    public GameField getGameField() {
	return gameField;
    }

    public void dispose() {
	SnakePlugin.trace(this.getClass().getName(),
		"Disposing Snake main view."); //$NON-NLS-1$

	// Dispose resources.
	SnakePlugin.getResourceManager().dispose();
    }
}