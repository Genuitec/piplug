package com.genuitec.piplug.tools.ui.views;

import java.text.DateFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.genuitec.piplug.tools.model.DeploymentStatus;
import com.genuitec.piplug.tools.model.PiPlugExtension;

public class ExtensionStateLabelProvider extends
		ColumnLabelProvider {
	private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	@Override
	public String getText(Object element) {
		if (element instanceof PiPlugExtension) {
			PiPlugExtension extension = (PiPlugExtension) element;
			DeploymentStatus status = extension.getDeploymentStatus();
			switch (status.getState()) {
				case NEVER_DEPLOYED:
					return "Never deployed";
				case DEPLOYED:
					return "Deployed on " + format.format(status.getDate());
			}
		}
		return super.getText(element);
	}
}
