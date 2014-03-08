package com.genuitec.piplug.client;

import java.util.ArrayList;
import java.util.Arrays;
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

    public Set<BundleDescriptor> matchesByIDVersion(
	    BundleDescriptor... matchDescriptors) {
	System.out.println("Current descriptors: " + descriptors);
	System.out.println("Looking for: " + Arrays.toString(matchDescriptors));
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

    @Override
    public boolean equals(Object obj) {
	if (null == obj)
	    return false;
	if (this == obj)
	    return true;
	if (!(obj instanceof BundleDescriptors))
	    return false;
	BundleDescriptors other = (BundleDescriptors) obj;
	return descriptors.equals(other.descriptors)
		&& syncTime == other.syncTime;
    }

    @Override
    public int hashCode() {
	return descriptors.hashCode() + (int) syncTime;
    }

    public Set<BundleDescriptor> matchesByIDVersion(BundleDescriptors other) {
	return matchesByIDVersion(other.getDescriptors().toArray(
		new BundleDescriptor[other.getDescriptors().size()]));
    }
}
