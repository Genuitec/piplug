package com.genuitec.piplug.tools.model;

import java.util.Date;

import com.genuitec.piplug.client.BundleDescriptor;

public class DeploymentStatus {

    private BundleDescriptor descriptor;

    public DeploymentStatus(BundleDescriptor descriptor) {
	this.descriptor = descriptor;
    }

    public DeploymentState getState() {
	return getDescriptor().getLastUpdatedOn() == null ? DeploymentState.NOT_DEPLOYED
		: DeploymentState.DEPLOYED;
    }

    private BundleDescriptor getDescriptor() {
	BundleDescriptor remoteDescriptor = PiPlugCore.getInstance()
		.getRemoteBundleDescriptor(descriptor);
	if (null != remoteDescriptor)
	    return remoteDescriptor;
	return descriptor;
    }

    public Date getDate() {
	return getDescriptor().getLastUpdatedOn();
    }
}
