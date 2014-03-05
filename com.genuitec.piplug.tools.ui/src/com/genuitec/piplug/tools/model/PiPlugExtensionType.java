package com.genuitec.piplug.tools.model;

public enum PiPlugExtensionType {
	APP("com.genuitec.piplug.api.app"), SERVICE("com.genuitec.piplug.api.service");
	
	private String extensionPointId;

	private PiPlugExtensionType(String extensionPointId) {
		this.extensionPointId = extensionPointId;
	}
	
	public String getExtensionPointId() {
		return extensionPointId;
	}
}
