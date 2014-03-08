package com.genuitec.piplug.common;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "descriptor")
public class BundleDescriptor {

    private String bundleID;
    private String version;
    private Date firstAdded;
    private Date lastUpdatedOn;
    private String appName;

    public BundleDescriptor() {
	// default constructor
    }

    public String getBundleID() {
	return bundleID;
    }

    public void setBundleID(String bundleID) {
	this.bundleID = bundleID;
    }

    public String getVersion() {
	return version;
    }

    public void setVersion(String version) {
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

    public String getAppName() {
	return appName;
    }

    public void setAppName(String appName) {
	this.appName = appName;
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
		&& nullSafeEquals(firstAdded, o.firstAdded)
		&& nullSafeEquals(lastUpdatedOn, o.lastUpdatedOn);
    }

    private boolean nullSafeEquals(Object o1, Object o2) {
	if (null == o1) {
	    if (null == o2)
		return true;
	    return false;
	}
	if (null == o2)
	    return false;
	return o1.equals(o2);
    }

    @Override
    public int hashCode() {
	return bundleID.hashCode() + 3 * version.hashCode() + 5
		* (firstAdded == null ? 0 : firstAdded.hashCode()) + 7
		* (lastUpdatedOn == null ? 0 : lastUpdatedOn.hashCode());
    }

    @Override
    public String toString() {
	return bundleID + "_" + version;
    }
}
