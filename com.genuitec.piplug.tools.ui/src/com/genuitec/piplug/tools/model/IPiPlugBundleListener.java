package com.genuitec.piplug.tools.model;

import com.genuitec.piplug.client.BundleDescriptors;

public interface IPiPlugBundleListener {

    void bundlesChanged(BundleDescriptors localBundleDescriptor,
	    BundleDescriptors remoteBundleDescriptor);

}
