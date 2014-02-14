/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package russotto.zplet.screenmodel;

import java.util.Vector;

import org.eclipse.swt.widgets.Display;

import russotto.zplet.zmachine.ZMachine;

@SuppressWarnings({ "rawtypes", "unchecked" })
class SyncVector extends Vector {
    private static final long serialVersionUID = 1615647740021244904L;

    public SyncVector() {
	super();
    }

    public synchronized Object syncPopFirstElement(ZMachine zm) {
	Object first = syncFirstElement(zm);
	if (first != null)
	    removeElementAt(0);
	return first;
    }

    public synchronized Object syncFirstElement(ZMachine zm) {
	Display display = Display.getDefault();
	while (zm.isRunning() && isEmpty() && !display.isDisposed()
		&& display.sleep()) {
	    while (zm.isRunning() && isEmpty() && display.readAndDispatch()) {
		// dispatch UI until an event
	    }
	}
	if (isEmpty())
	    return null;
	return super.firstElement();
    }

    public synchronized void syncAddElement(Object obj) {
	super.addElement(obj);
	Display.getDefault().wake();
    }
}
