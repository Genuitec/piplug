package com.genuitec.piplug.tools.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.genuitec.piplug.tools.model.PiPlugExtension;
import com.genuitec.piplug.tools.model.PiPlugBundle;

public class ExtensionBundleLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof PiPlugExtension) {
			PiPlugExtension extension = (PiPlugExtension) element;
			PiPlugBundle bundle = extension.getBundle();
			if (bundle == null)
				return "unknown";
			return bundle.getDescriptor().getBundleID();
		}
		return "";
	}
}
