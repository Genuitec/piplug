/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import russotto.zplet.screenmodel.ZScreen;
import russotto.zplet.screenmodel.ZStatus;
import russotto.zplet.zmachine.ZMachine;
import russotto.zplet.zmachine.zmachine3.ZMachine3;
import russotto.zplet.zmachine.zmachine5.ZMachine5;
import russotto.zplet.zmachine.zmachine5.ZMachine8;

public class ZJApp {
    ZScreen screen;
    ZStatus status_line;
    ZMachine zm;
    static String pstatusfg, pstatusbg, pmainfg, pmainbg;
    static String pzcodefile = null;
    String statusfg, statusbg, mainfg, mainbg;
    String zcodefile = null;

    boolean failed = false;
    private Shell shell;

    public synchronized static void main(String argv[]) {
	int i;
	ZJApp myz;

	for (i = 0; i < argv.length; i++) {
	    if (argv[i].charAt(0) == '-') {
		switch (argv[i].charAt(1)) {
		case 'f':
		    pmainfg = pstatusbg = argv[++i];
		    break;

		case 'b':
		    pmainbg = pstatusfg = argv[++i];
		    break;

		default:
		    break;
		}
	    } else
		pzcodefile = argv[i];
	}
	if (pzcodefile == null) {
	    System.err.println("Path to game file must be supplied");
	    return;
	}

	myz = new ZJApp(pzcodefile, pstatusfg, pstatusbg, pmainfg, pmainbg);
	myz.shell.setBounds(50, 50, 1200, 800);
	myz.shell.open();
	myz.start();
	System.out.println("Done");
    }

    ZJApp() {
	this(null, null, null, null, null);
    }

    ZJApp(String pzcodefile, String pstatusfg, String pstatusbg,
	    String pmainfg, String pmainbg) {
	mainfg = pmainfg;
	mainbg = pmainbg;
	statusfg = pstatusfg;
	statusbg = pstatusbg;
	zcodefile = pzcodefile;

	this.shell = new Shell(Display.getDefault(), SWT.CLOSE);
	GridLayout layout = new GridLayout(1, false);
	layout.marginWidth = layout.marginHeight = 20;
	layout.verticalSpacing = 8;
	shell.setLayout(layout);
	shell.setBackground(ZColor.getcolor(ZColor.Z_BLACK));

	status_line = new ZStatus(shell);
	status_line.setForeground(ZColor.getcolor(ZColor.Z_GREEN));
	status_line.setBackground(ZColor.getcolor(ZColor.Z_BLACK));
	status_line.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

	screen = new ZScreen(shell);
	screen.setZForeground(ZColor.Z_GREEN);
	screen.setZBackground(ZColor.Z_BLACK);
	screen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	status_line.setFont(screen.getFixedFont());

	if (statusfg != null)
	    status_line.setForeground(ZColor.getcolor(statusfg));

	if (statusbg != null)
	    status_line.setBackground(ZColor.getcolor(statusbg));

	if (mainfg != null)
	    screen.setZForeground(ZColor.getcolornumber(mainfg));
	if (mainbg != null)
	    screen.setZBackground(ZColor.getcolornumber(mainbg));
    }

    void startzm() {
	URL myzzurl;
	InputStream myzstream;

	byte zmemimage[];

	zmemimage = null;
	try {
	    System.err.println("reading: " + zcodefile);
	    myzzurl = new URL(zcodefile);
	    myzstream = myzzurl.openStream();
	    zmemimage = suckstream(myzstream);
	} catch (MalformedURLException booga) {
	    try {
		myzstream = new FileInputStream(zcodefile);
		zmemimage = suckstream(myzstream);
	    } catch (IOException booga2) {
		Label label = new Label(shell, SWT.CENTER);
		label.setText("Malformed URL");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true,
			false));
		shell.layout(true);
		failed = true;
	    }
	} catch (IOException booga) {
	    Label label = new Label(shell, SWT.CENTER);
	    label.setText("IO Error");
	    label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true,
		    false));
	    shell.layout(true);
	    /* don't set failed, may want to retru */
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
		Label label = new Label(shell, SWT.CENTER);
		label.setText("Not a valid V3, V5, or V8 story file");
		label.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true,
			false));
		shell.layout(true);
	    }
	    if (zm != null)
		zm.runInMain();
	}
    }

    byte[] suckstream(InputStream mystream) throws IOException {
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

    public void start() {
	if (!failed && ((zm == null) || !zm.isAlive())) {
	    startzm();
	}

    }

    public void destroy() {
	zm.terminate();
	zm = null;
	screen.dispose();
	screen = null;
    }

}
