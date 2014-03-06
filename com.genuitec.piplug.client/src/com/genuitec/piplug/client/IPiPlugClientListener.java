package com.genuitec.piplug.client;

public interface IPiPlugClientListener {
    void bundleAdded(BundleDescriptor descriptor);

    void bundleChanged(BundleDescriptor descriptor);

    void bundleRemoved(BundleDescriptor descriptor);

}
