package com.genuitec.piplug.client;

import java.util.Date;

import org.osgi.framework.Version;

public class BundleDescriptor {

    private String bundleID;
    private Version version;
    private Date firstAdded;
    private Date lastUpdatedOn;

    public BundleDescriptor(String bundleID, Version version, Date firstAdded,
	    Date lastUpdatedOn) {
	this.bundleID = bundleID;
	this.version = version;
	this.firstAdded = firstAdded;
	this.lastUpdatedOn = lastUpdatedOn;
    }

    public String getBundleID() {
	return bundleID;
    }

    public void setBundleID(String bundleID) {
	this.bundleID = bundleID;
    }

    public Version getVersion() {
	return version;
    }

    public void setVersion(Version version) {
	this.version = version;
    }

    public Date getFirstAdded() {
	return firstAdded;
    }

    public void setFirstAdded(Date firstAdded) {
	this.firstAdded = firstAdded;
    }

    public Date getLastUpdatedOn() {
	return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn) {
	this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Returns true if this object is the same bundle ID as the other.
     */
    public boolean matchesID(BundleDescriptor o) {
	return bundleID.equals(o.bundleID);
    }

    /**
     * Returns true if this object is the same bundle ID and version as the
     * other.
     */
    public boolean matchesIDVersion(BundleDescriptor o) {
	return bundleID.equals(o.bundleID) && version.equals(o.version);
    }

    /**
     * Returns true if this object is the same bundle ID and version as the
     * other.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof BundleDescriptor))
	    return false;
	BundleDescriptor o = (BundleDescriptor) obj;
	return bundleID.equals(o.bundleID) && version.equals(o.version)
		&& firstAdded.equals(o.firstAdded)
		&& lastUpdatedOn.equals(o.lastUpdatedOn);
    }
}
