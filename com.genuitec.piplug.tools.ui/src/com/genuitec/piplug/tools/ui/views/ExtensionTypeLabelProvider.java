package com.genuitec.piplug.tools.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.genuitec.piplug.tools.model.PiPlugExtension;

public class ExtensionTypeLabelProvider extends ColumnLabelProvider {

    public ExtensionTypeLabelProvider() {
    }

    public String getText(Object obj) {
	if (obj instanceof PiPlugExtension) {
	    PiPlugExtension app = (PiPlugExtension) obj;
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