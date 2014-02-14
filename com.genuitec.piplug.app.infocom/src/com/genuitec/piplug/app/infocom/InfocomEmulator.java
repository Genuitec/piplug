package com.genuitec.piplug.app.infocom;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

import com.genuitec.piplug.api.IPiPlugAppBranding;
import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.PiPlugAppBranding;

public abstract class InfocomEmulator implements IPiPlugApplication {

    private InfocomComposite composite;
    private InfocomEngine engine;
    private String bundleID;
    private PiPlugAppBranding branding;
    private String gameFile;

    public InfocomEmulator(String bundleID, String appName, String gameFile) {
	this.bundleID = bundleID;
	this.branding = new PiPlugAppBranding(bundleID, appName);
	this.gameFile = gameFile;
    }

    @Override
    public IPiPlugAppBranding getBranding() {
	return branding;
    }

    @Override
    public void installed(IPiPlugServices services) {
	// no loading is needed at installation time
    }

    @Override
    public Composite prepare(IPiPlugServices services, Composite parentStack) {
	composite = new InfocomComposite(parentStack);
	engine = new InfocomEngine(services, composite);
	return composite;
    }

    @Override
    public void resume(IPiPlugServices services) {
	Bundle bundle = getBundle();
	engine.start(bundle.getEntry(gameFile));
    }

    @Override
    public void suspend(IPiPlugServices services) {
	engine.stop();
    }

    @Override
    public void shutdown(IPiPlugServices services) {
	// no unloading is needed at shutdown
    }

    private Bundle getBundle() {
	return Platform.getBundle(bundleID);
    }
}
