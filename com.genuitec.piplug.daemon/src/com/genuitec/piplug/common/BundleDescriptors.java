package com.genuitec.piplug.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "descriptors")
public class BundleDescriptors {

    private List<BundleDescriptor> descriptors = new ArrayList<BundleDescriptor>();

    @XmlElement(name = "descriptor")
    public List<BundleDescriptor> getDescriptors() {
	return descriptors;
    }

    public void setDescriptors(List<BundleDescriptor> descriptors) {
	this.descriptors = descriptors;
    }
}
