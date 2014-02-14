package com.genuitec.piplug.ui;

import java.util.Comparator;

import com.genuitec.piplug.api.IPiPlugApplication;

public class PiPlugAppComparator implements Comparator<IPiPlugApplication> {

    @Override
    public int compare(IPiPlugApplication app1, IPiPlugApplication app2) {
	return app1.getBranding().getName()
		.compareTo(app2.getBranding().getName());
    }
}
