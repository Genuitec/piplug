package com.genuitec.piplug.tools.model;

public interface IPiPlugApplicationListener {

	void bundleAdded(PiPlugBundle bundle);

	void bundleChanged(PiPlugBundle bundle);

	void bundleRemoved(PiPlugBundle bundle);

}
