package com.genuitec.piplug.tools.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class DeployView extends ViewPart {

	public enum AppsViewColumn {
		APPLICATION, TYPE, BUNDLE, STATE
	}

	public static final String ID = "com.genuitec.piplug.tools.ui.views.PiPlugAppsView";

	private TableViewer viewer;
	private Action deployAction;
	private Action runLocallyAction;

	private ImageRegistry imageRegistry;

	private List<TableViewerColumn> columns;

	// required for extension point
	public DeployView() {
	}

	public void createPartControl(Composite parent) {
		imageRegistry = new ImageRegistry(parent.getDisplay());
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		createTableColumns();
		viewer.setContentProvider(new DeployContentProvider(this));
		viewer.setComparator(new ExtensionNameComparator());
		getSite().setSelectionProvider(viewer);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		makeActions();
		hookContextMenu();
		contributeToActionBars();

		viewer.setInput(getViewSite());
	}

	void packColumns() {
		for (TableViewerColumn column : columns) {
			AppsViewColumn viewColumn = (AppsViewColumn) column.getColumn().getData("id");
			if (AppsViewColumn.TYPE.equals(viewColumn)) {
				column.getColumn().pack();
			}
		}
	}

	private void createTableColumns() {
		columns = new ArrayList<TableViewerColumn>();
		TableViewerColumn appColumn = new TableViewerColumn(viewer, SWT.NONE);
		appColumn.getColumn().setWidth(200);
		appColumn.getColumn().setText("Application");
		appColumn.setLabelProvider(new ExtensionNameLabelProvider(
				imageRegistry));
		appColumn.getColumn().setData("id", AppsViewColumn.APPLICATION);

		TableViewerColumn typeColumn = new TableViewerColumn(viewer, SWT.NONE);
		typeColumn.getColumn().setWidth(60);
		typeColumn.getColumn().setText("Type");
		typeColumn.setLabelProvider(new ExtensionTypeLabelProvider());
		appColumn.getColumn().setData("id", AppsViewColumn.TYPE);

		TableViewerColumn bundleColumn = new TableViewerColumn(viewer, SWT.NONE);
		bundleColumn.getColumn().setWidth(300);
		bundleColumn.getColumn().setText("Bundle");
		bundleColumn.setLabelProvider(new ExtensionBundleLabelProvider());
		appColumn.getColumn().setData("id", AppsViewColumn.BUNDLE);

		TableViewerColumn statusColumn = new TableViewerColumn(viewer, SWT.NONE);
		statusColumn.getColumn().setWidth(200);
		statusColumn.getColumn().setText("State");
		statusColumn.setLabelProvider(new ExtensionStateLabelProvider());
		appColumn.getColumn().setData("id", AppsViewColumn.STATE);

	}

	@Override
	public void dispose() {
		super.dispose();
		if (null != imageRegistry) {
			imageRegistry.dispose();
			imageRegistry = null;
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DeployView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(deployAction);
		manager.add(new Separator());
		manager.add(runLocallyAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(deployAction);
		manager.add(new Separator());
		manager.add(runLocallyAction);
	}

	private void makeActions() {
		deployAction = new DeployAction(getSite());
		runLocallyAction = new RunLocallyAction(getSite());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}