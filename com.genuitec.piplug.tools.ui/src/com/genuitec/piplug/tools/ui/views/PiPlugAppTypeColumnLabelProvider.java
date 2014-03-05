package com.genuitec.piplug.tools.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;

public class PiPlugAppTypeColumnLabelProvider extends ColumnLabelProvider {

	public PiPlugAppTypeColumnLabelProvider() {
	}

	public String getText(Object obj) {
		if (obj instanceof PiPlugApplicationExtension) {
			PiPlugApplicationExtension app = (PiPlugApplicationExtension) obj;
			switch (app.getType()) {
			case APP:
				return "App";
			case SERVICE:
				return "Service";
			}
		}
		return getText(obj);
	}
}