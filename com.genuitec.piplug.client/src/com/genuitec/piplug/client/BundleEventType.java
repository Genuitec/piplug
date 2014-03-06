package com.genuitec.piplug.client;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "bundle-event-type")
@XmlEnum
public enum BundleEventType {
    ADDED, CHANGED, REMOVED;
}
