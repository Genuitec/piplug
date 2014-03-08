package com.genuitec.piplug.app.snake;

import org.eclipse.swt.widgets.Composite;

import com.genuitec.piplug.api.IPiPlugAppBranding;
import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.PiPlugAppBranding;

import es.org.chemi.games.snake.SnakePlugin;
import es.org.chemi.games.snake.ui.MainView;
import es.org.chemi.games.snake.util.Constants;

public class SnakeApplication implements IPiPlugApplication {

    private MainView mainView;
    private boolean suspended;

    @Override
    public IPiPlugAppBranding getBranding() {
	return new PiPlugAppBranding("com.genuitec.piplug.app.snake", "Snake");
    }

    @Override
    public void installed(IPiPlugServices services) {
	// nothing to do right now
    }

    @Override
    public Composite prepare(IPiPlugServices services, Composite parentStack) {
	mainView = new MainView(services);
	return mainView.createComposite(parentStack);
    }

    @Override
    public void resume(IPiPlugServices services) {
	SnakePlugin.getResourceManager().getSound(Constants.SOUND_START).play();
	if (suspended) {
	    // user can resume by key stroke
	    mainView.setFocus();
	} else {
	    mainView.getGameField().stopGame(false);
	    mainView.getGameField().resetGame();
	    mainView.getGameField().createGameField();
	    mainView.setFocus();
	}
    }

    @Override
    public void suspend(IPiPlugServices services) {
	if (mainView == null || mainView.getGameField() == null)
	    return;
	suspended = !mainView.getGameField().isGamePaused();
	mainView.getGameField().pauseGame();
    }

    @Override
    public void shutdown(IPiPlugServices services) {
	if (mainView != null)
	    mainView.dispose();
	mainView = null;
    }
}