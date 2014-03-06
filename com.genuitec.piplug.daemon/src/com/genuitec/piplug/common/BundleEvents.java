package com.genuitec.piplug.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "events")
public class BundleEvents {
    private List<BundleEvent> events = new ArrayList<BundleEvent>();

    @XmlElement(name = "event")
    public List<BundleEvent> getEvents() {
	return events;
    }

    public void setEvents(List<BundleEvent> events) {
	this.events = events;
    }

}