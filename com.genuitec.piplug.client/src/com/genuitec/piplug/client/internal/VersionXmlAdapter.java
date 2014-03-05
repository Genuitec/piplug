package com.genuitec.piplug.client.internal;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.osgi.framework.Version;

public class VersionXmlAdapter extends XmlAdapter<String, Version> {

	@Override
	public Version unmarshal(String versionString) throws Exception {
		if (versionString == null || versionString.length() == 0)
			return null;
		return Version.parseVersion(versionString);
	}

	@Override
	public String marshal(Version version) throws Exception {
		if (version == null)
			return null;
		return version.toString();
	}
}