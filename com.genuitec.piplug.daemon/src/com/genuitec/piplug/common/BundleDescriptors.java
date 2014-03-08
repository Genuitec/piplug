package com.genuitec.piplug.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "descriptors")
public class BundleDescriptors {

    private List<BundleDescriptor> descriptors = new ArrayList<BundleDescriptor>();
    private long syncTime;

    @XmlElement(name = "descriptor")
    public List<BundleDescriptor> getDescriptors() {
	return descriptors;
    }

    public void setDescriptors(List<BundleDescriptor> descriptors) {
	this.descriptors = descriptors;
    }

    public long getSyncTime() {
	return syncTime;
    }

    public void setSyncTime(long syncTime) {
	this.syncTime = syncTime;
    }

    @Override
    public Object clone() {
	synchronized (this) {
	    BundleDescriptors clone = new BundleDescriptors();
	    clone.setDescriptors(new ArrayList<BundleDescriptor>(descriptors));
	    clone.setSyncTime(syncTime);
	    return clone;
	}
    }

    public BundleDescriptor matchByIDVersion(BundleDescriptor matchDescriptor) {
	for (BundleDescriptor candidate : descriptors) {
	    if (matchDescriptor.matchesIDVersion(candidate)) {
		return candidate;
	    }
	}
	return null;
    }

    public Set<BundleDescriptor> matchesByIDVersion(
	    BundleDescriptor... matchDescriptors) {
	Set<BundleDescriptor> matches = new HashSet<BundleDescriptor>();
	for (BundleDescriptor candidate : descriptors) {
	    for (BundleDescriptor next : matchDescriptors) {
		if (next.matchesIDVersion(candidate)) {
		    matches.add(candidate);
		    break;
		}
	    }
	}
	return matches;
    }

    public Set<BundleDescriptor> matchesByID(
	    BundleDescriptor... matchDescriptors) {
	Set<BundleDescriptor> matches = new HashSet<BundleDescriptor>();
	for (BundleDescriptor candidate : descriptors) {
	    for (BundleDescriptor next : matchDescriptors) {
		if (next.matchesID(candidate)) {
		    matches.add(candidate);
		    break;
		}
	    }
	}
	return matches;
    }

    public boolean remove(BundleDescriptor descriptor) {
	return descriptors.remove(descriptor);
    }

    public void add(BundleDescriptor descriptor) {
	descriptors.add(descriptor);
    }
}
