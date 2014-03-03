package com.genuitec.piplug.tools.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;
import com.genuitec.piplug.tools.model.PiPlugBundle;

public class PiPlugBundleColumnLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof PiPlugApplicationExtension) {
			PiPlugApplicationExtension app = (PiPlugApplicationExtension) element;
			PiPlugBundle bundle = app.getBundle();
			if (bundle == null)
				return "unknown";
			return bundle.getBundleId();
		}
		return "";
	}
}
