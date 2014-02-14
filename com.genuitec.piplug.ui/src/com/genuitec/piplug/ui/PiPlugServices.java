package com.genuitec.piplug.ui;

import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.IPiPlugUITheme;

public class PiPlugServices implements IPiPlugServices {

    private IPiPlugUITheme theme;

    public PiPlugServices(IPiPlugUITheme theme) {
	this.theme = theme;
    }

    @Override
    public IPiPlugUITheme getGlobalTheme() {
	return theme;
    }
}
