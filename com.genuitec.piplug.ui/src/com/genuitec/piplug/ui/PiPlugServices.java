package com.genuitec.piplug.ui;

import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugServices implements IPiPlugServices {

    private IPiPlugUITheme theme;
    private PiPlugAppContainer container;

    public PiPlugServices(IPiPlugUITheme theme) {
	this.theme = theme;
    }

    public void setContainer(PiPlugAppContainer container) {
	this.container = container;
    }

    @Override
    public IPiPlugUITheme getGlobalTheme() {
	return theme;
    }

    @Override
    public void switchToHome() {
	this.container.switchToHome();
    }
}
