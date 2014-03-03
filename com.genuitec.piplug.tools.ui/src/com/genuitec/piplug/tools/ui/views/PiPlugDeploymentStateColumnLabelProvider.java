package com.genuitec.piplug.tools.ui.views;

import java.text.DateFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;
import com.genuitec.piplug.tools.model.PiPlugDeploymentState;
import com.genuitec.piplug.tools.model.PiPlugDeploymentStatus;

public class PiPlugDeploymentStateColumnLabelProvider extends
		ColumnLabelProvider {
	private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	@Override
	public String getText(Object element) {
		if (element instanceof PiPlugApplicationExtension) {
			PiPlugApplicationExtension app = (PiPlugApplicationExtension) element;
			PiPlugDeploymentState state = app.getDeploymentState();
			PiPlugDeploymentStatus status = state.getStatus();
			switch (status) {
				case NEVER_DEPLOYED:
					return "Never deployed";
				case STALE:
					return "Stale, last deployment on " + format.format(state.getDate());
				case DEPLOYED:
					return "Deployed on " + format.format(state.getDate());
			}
		}
		return super.getText(element);
	}
}
