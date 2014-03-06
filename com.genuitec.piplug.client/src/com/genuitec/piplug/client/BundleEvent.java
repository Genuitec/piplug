package com.genuitec.piplug.client;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "bundle-event")
public class BundleEvent {

    private BundleEventType type;
    private BundleDescriptor descriptor;
    private long timestamp;

    public BundleEvent() {
	this.timestamp = System.currentTimeMillis();
    }

    public BundleEvent(BundleEventType type, BundleDescriptor descriptor) {
	this.type = type;
	this.descriptor = descriptor;
	this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
	return timestamp;
    }

    public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;
    }

    public BundleEventType getType() {
	return type;
    }

    public void setType(BundleEventType type) {
	this.type = type;
    }

    public BundleDescriptor getDescriptor() {
	return descriptor;
    }

    public void setDescriptor(BundleDescriptor descriptor) {
	this.descriptor = descriptor;
    }

    public long getAge() {
	return System.currentTimeMillis() - timestamp;
    }

    public boolean occuredAfter(long date) {
	return timestamp <= date;
    }

}
