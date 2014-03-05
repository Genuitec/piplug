package com.genuitec.piplug.tools.operations;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Version;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;
import com.genuitec.piplug.tools.model.PiPlugBundle;
import com.genuitec.piplug.tools.model.PiPlugCore;
import com.genuitec.piplug.tools.ui.Activator;

@SuppressWarnings("restriction")
public class PiPlugDeployOperation {

	private Set<PiPlugApplicationExtension> selectedApps;

	public PiPlugDeployOperation(Set<PiPlugApplicationExtension> selectedApps) {
		this.selectedApps = selectedApps;
	}

	public void run() {
		buildPlugins();
	}

	private void buildPlugins() {
		SortedSet<PiPlugBundle> sourceBundles = new TreeSet<PiPlugBundle>();
		final SortedSet<PiPlugBundle> binaryBundles = new TreeSet<PiPlugBundle>();
		bucketSelectedApps(sourceBundles, binaryBundles);
		final FeatureExportInfo info;
		try {
			info = createExportInfo(sourceBundles);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		final PluginExportOperation job = new PluginExportOperation(info,
				"Exporting apps");
		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (job.hasAntErrors()) {
					// If there were errors when running the ant scripts, inform
					// the user where the logs can be found.
					final File logLocation = new File(
							info.destinationDirectory, "logs.zip"); //$NON-NLS-1$
					if (logLocation.exists()) {
						reportExportError(logLocation);
					} else {
						reportError(
								"An unexpected error occurred during the build phase of deployment.",
								job.getResult());
					}
				} else if (event.getResult().isOK()) {
					deployBundles(info, binaryBundles);
				}
			}
		});
		job.schedule();
	}

	protected void deployBundles(FeatureExportInfo info,
			SortedSet<PiPlugBundle> binaryBundles) {
		File exportDir = new File(info.destinationDirectory);
		File pluginsDir = new File(exportDir, "plugins");

		PiPlugClient client = new PiPlugClient();
		InetSocketAddress serverAddress;
		try {
			serverAddress = client.discoverServer(30000);
		} catch (CoreException e) {
			reportError(
					"Could not discover the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return;
		}

		try {
			client.connectTo(serverAddress);
		} catch (CoreException e) {
			reportError(
					"Could not connect to the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return;
		}

		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 42,
				"Deployment of bundles report", null);

		try {
			deployBuiltBundles(pluginsDir, client, status);
			deployBinaryBundles(binaryBundles, client, status);
		} finally {
			if (status.isOK()) {
				if (PiPlugCore.isDebug())
					Activator.getDefault().getLog().log(status);
			} else {
				reportError(
						"Errors occured during the upload phase of deployment",
						status);
			}

			try {
				client.disconnect();
			} catch (CoreException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
								"Could not disconnect from the PiPlug Daemon.",
								e));
			}
		}
	}

	private void deployBinaryBundles(SortedSet<PiPlugBundle> binaryBundles,
			PiPlugClient client, MultiStatus status) {
		// TODO Auto-generated method stub

	}

	private void deployBuiltBundles(File pluginsDir, PiPlugClient client,
			MultiStatus status) {
		File[] plugins = pluginsDir.listFiles();
		for (File file : plugins) {
			String nameAndVersion = file.getName();
			int underscoreIndex = nameAndVersion.indexOf('_');
			int dotIndex = nameAndVersion.lastIndexOf('.');
			String id = nameAndVersion.substring(0, underscoreIndex);
			String version = nameAndVersion.substring(underscoreIndex + 1,
					dotIndex);
			BundleDescriptor descriptor = new BundleDescriptor(id,
					Version.parseVersion(version), null, null);
			try {
				client.uploadBundle(descriptor, file);
				status.add(new Status(IStatus.INFO, Activator.PLUGIN_ID,
						"Deployed " + descriptor, null));
			} catch (CoreException e) {
				status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Failed to deploy " + descriptor, e));
			}
		}
	}

	private void bucketSelectedApps(SortedSet<PiPlugBundle> sourceBundles,
			SortedSet<PiPlugBundle> binaryBundles) {
		for (PiPlugApplicationExtension app : selectedApps) {
			PiPlugBundle bundle = app.getBundle();
			IProject project = bundle.getProject();
			if (null == project) {
				IModel model = bundle.getPlugin();
				if (model == null) {
					binaryBundles.add(bundle);
				} else {
					sourceBundles.add(bundle);
				}
			} else {
				sourceBundles.add(bundle);
			}
		}
	}

	private FeatureExportInfo createExportInfo(
			SortedSet<PiPlugBundle> sourceBundles) throws IOException {
		File exportDir = File.createTempFile("piplug-deploy-", ".d");
		exportDir.delete();

		final FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.useJarFormat = true;
		info.exportSource = false;
		info.exportSourceBundle = false;
		info.allowBinaryCycles = true;
		info.useWorkspaceCompiledClasses = false;
		info.destinationDirectory = exportDir.getAbsolutePath();
		info.zipFileName = null;
		info.items = toPluginModelArray(sourceBundles);
		info.signingInfo = null;
		info.qualifier = null;

		return info;
	}

	private Object[] toPluginModelArray(SortedSet<PiPlugBundle> bundles) {
		ArrayList<IPluginModelBase> result = new ArrayList<IPluginModelBase>();
		for (PiPlugBundle bundle : bundles) {
			IPluginModelBase plugin = bundle.getPlugin();
			if (null != plugin) {
				result.add(plugin);
			}
		}
		return result.toArray();
	}

	protected void reportError(final String message, final IStatus status) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(null, "Deploy Error", message, status);
			}
		});
	}

	protected void reportExportError(final File logLocation) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(null, "Deploy Error",
						"Export has failed during deployment.\\n\\nSee the error log at "
								+ logLocation.getAbsolutePath(), null);
			}
		});
	}
}
