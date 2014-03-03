package com.genuitec.piplug.tools.model;

import java.util.Date;

public class PiPlugDeploymentState {

	private final PiPlugDeploymentStatus status;
	private final Date date;

	public PiPlugDeploymentState(PiPlugDeploymentStatus status) {
		this(status, null);
	}
	
	public PiPlugDeploymentState(PiPlugDeploymentStatus status, Date date) {
		this.status = status;
		this.date = date;
	}

	public PiPlugDeploymentStatus getStatus() {
		return status;
	}

	public Date getDate() {
		return date;
	}
}
