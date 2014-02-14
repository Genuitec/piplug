package com.genuitec.piplug.app.clock;

import org.eclipse.swt.widgets.Composite;

import com.genuitec.piplug.api.IPiPlugAppBranding;
import com.genuitec.piplug.api.IPiPlugApplication;
import com.genuitec.piplug.api.IPiPlugServices;
import com.genuitec.piplug.api.PiPlugAppBranding;

public class ClockUtility implements IPiPlugApplication {

	private ClockComposite composite;
	private ClockJob job;

	@Override
	public IPiPlugAppBranding getBranding() {
		return new PiPlugAppBranding("com.genuitec.piplug.app.clock", "Clock Utility");
	}

	@Override
	public void installed(IPiPlugServices services) {
		// nothing needed at installation time
	}

	@Override
	public Composite prepare(IPiPlugServices services, Composite parentStack) {
		composite = new ClockComposite(services.getGlobalTheme(), parentStack);
		return composite;
	}

	@Override
	public void resume(IPiPlugServices services) {
		job = new ClockJob(composite);
	}

	@Override
	public void suspend(IPiPlugServices services) {
		job.stop();
	}

	@Override
	public void shutdown(IPiPlugServices services) {
		// nothing needed during shutdown
	}
}