package com.genuitec.piplug.api;

import org.eclipse.swt.widgets.Composite;

public interface IPiPlugApplication {

    public IPiPlugAppBranding getBranding();

    public void installed(IPiPlugServices services);

    public Composite prepare(IPiPlugServices services, Composite parentStack);

    public void resume(IPiPlugServices services);

    public void suspend(IPiPlugServices services);

    public void shutdown(IPiPlugServices services);
}
