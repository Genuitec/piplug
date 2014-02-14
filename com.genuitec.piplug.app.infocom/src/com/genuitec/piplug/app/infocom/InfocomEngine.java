package com.genuitec.piplug.app.infocom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;
import russotto.zplet.zmachine.ZMachine;
import russotto.zplet.zmachine.zmachine3.ZMachine3;
import russotto.zplet.zmachine.zmachine5.ZMachine5;
import russotto.zplet.zmachine.zmachine5.ZMachine8;

import com.genuitec.piplug.api.IPiPlugServices;

public class InfocomEngine {

    private final class RunEmulation implements Runnable {
	public void run() {
	    screen.zm = zm;
	    screen.setFocus();
	    zm.runInMain();
	    screen.clear();
	    status_line.clear();
	    services.switchToHome();
	}
    }

    private InfocomComposite composite;
    private ZMachine zm;
    private ZStatus status_line;
    private ZScreen screen;
    private IPiPlugServices services;

    public InfocomEngine(IPiPlugServices services, InfocomComposite composite) {
	this.composite = composite;
	this.status_line = composite.getStatusLine();
	this.screen = composite.getScreen();
	this.services = services;
    }

    public void start(URL zcodefile) {
	if ((zm != null) && zm.isAlive()) {
	    stop();
	    zm = null;
	}
	startzm(zcodefile);
	composite.getDisplay().asyncExec(new RunEmulation());
    }

    public void stop() {
	zm.terminate();
	zm = null;
    }

    private void startzm(URL zcodefile) {
	InputStream myzstream;
	byte zmemimage[];
	zmemimage = null;
	try {
	    myzstream = zcodefile.openStream();
	    try {
		zmemimage = suckstream(myzstream);
	    } finally {
		myzstream.close();
	    }
	} catch (IOException booga) {
	    Label label = new Label(composite, SWT.CENTER);
	    label.setText("IO Error");
	    label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true,
		    false));
	    composite.layout(true);
	    /* don't set failed, may want to retry */
	}
	if (zmemimage != null) {
	    switch (zmemimage[0]) {
	    case 3:
		zm = new ZMachine3(screen, status_line, zmemimage);
		break;
	    case 5:
		status_line.dispose();
		zm = new ZMachine5(screen, zmemimage);
		break;
	    case 8:
		status_line.dispose();
		zm = new ZMachine8(screen, zmemimage);
		break;
	    default:
		Label label = new Label(composite, SWT.CENTER);
		label.setText("Not a valid V3, V5, or V8 story file");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true,
			false));
		composite.layout(true);
	    }
	}
    }

    private byte[] suckstream(InputStream mystream) throws IOException {
	byte buffer[];
	byte oldbuffer[];
	int currentbytes = 0;
	int bytesleft;
	int got;
	int buffersize = 2048;

	buffer = new byte[buffersize];
	bytesleft = buffersize;
	got = 0;
	while (got != -1) {
	    bytesleft -= got;
	    currentbytes += got;
	    if (bytesleft == 0) {
		oldbuffer = buffer;
		buffer = new byte[buffersize + currentbytes];
		System.arraycopy(oldbuffer, 0, buffer, 0, currentbytes);
		oldbuffer = null;
		bytesleft = buffersize;
	    }
	    got = mystream.read(buffer, currentbytes, bytesleft);
	}
	if (buffer.length != currentbytes) {
	    oldbuffer = buffer;
	    buffer = new byte[currentbytes];
	    System.arraycopy(oldbuffer, 0, buffer, 0, currentbytes);
	}
	return buffer;
    }
}