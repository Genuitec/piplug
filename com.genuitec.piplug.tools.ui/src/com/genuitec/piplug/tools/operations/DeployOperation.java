package com.genuitec.piplug.tools.operations;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugBundle;
import com.genuitec.piplug.tools.model.PiPlugCore;
import com.genuitec.piplug.tools.model.PiPlugExtension;
import com.genuitec.piplug.tools.ui.Activator;

@SuppressWarnings("restriction")
public class DeployOperation extends PiPlugOperation {

	private Set<PiPlugExtension> extensions;

	public DeployOperation(Set<PiPlugExtension> extensions) {
		super("Deploying applications");
		this.extensions = extensions;
	}

	@Override
	protected IStatus doRun(final IProgressMonitor monitor) {
		final Set<PiPlugBundle> sourceBundles = new HashSet<PiPlugBundle>();
		final Set<PiPlugBundle> binaryBundles = new HashSet<PiPlugBundle>();
		bucketSelectedExtensions(sourceBundles, binaryBundles);

		monitor.beginTask("Deploying applications", sourceBundles.size() * 2
				+ binaryBundles.size());

		final FeatureExportInfo info;
		try {
			info = createExportInfo(sourceBundles);
		} catch (IOException e) {
			e.printStackTrace();
			monitor.done();
			return Status.OK_STATUS;
		}

		final PluginExportOperation job = new PluginExportOperation(info,
				"Exporting apps");
		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				monitor.worked(sourceBundles.size());
				if (job.hasAntErrors()) {
					// If there were errors when running the ant scripts, inform
					// the user where the logs can be found.
					final File logLocation = new File(
							info.destinationDirectory, "logs.zip"); //$NON-NLS-1$
					if (logLocation.exists()) {
						reportExportError(logLocation);
					} else {
						reportError(
								"Deploy Error",
								"An unexpected error occurred during the build phase of deployment.",
								job.getResult());
					}
				} else if (event.getResult().isOK()) {
					deployBundles(info, sourceBundles, binaryBundles, monitor);
				}
			}
		});
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.done();
		}

		return Status.OK_STATUS;
	}

	protected void deployBundles(FeatureExportInfo info,
			Set<PiPlugBundle> sourceBundles, Set<PiPlugBundle> binaryBundles,
			IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return;
		monitor.subTask("Uploading applications");
		File exportDir = new File(info.destinationDirectory);
		File pluginsDir = new File(exportDir, "plugins");

		PiPlugClient client = new PiPlugClient();
		InetSocketAddress serverAddress;
		try {
			serverAddress = client.discoverServer(30000);
		} catch (CoreException e) {
			reportError(
					"Deploy Error",
					"Could not discover the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return;
		}

		try {
			client.connectTo(serverAddress);
		} catch (CoreException e) {
			reportError(
					"Deploy Error",
					"Could not connect to the PiPlug Daemon.\n\nAre you sure you have one running on your local network?",
					e.getStatus());
			return;
		}

		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 42,
				"Deployment of bundles report", null);

		monitor.beginTask("Deploying applications", sourceBundles.size()
				+ binaryBundles.size());
		try {
			deployBuiltBundles(sourceBundles, pluginsDir, client, status,
					monitor);
			deployBinaryBundles(binaryBundles, client, status, monitor);

			try {
				client.notifyClients();
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
		} finally {
			if (status.isOK()) {
				if (PiPlugCore.isDebug())
					Activator.getDefault().getLog().log(status);
			} else {
				reportError("Deploy Error",
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

	private void deployBinaryBundles(Set<PiPlugBundle> binaryBundles,
			PiPlugClient client, MultiStatus status, IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	private void deployBuiltBundles(Set<PiPlugBundle> sourceBundles,
			File pluginsDir, PiPlugClient client, MultiStatus status,
			IProgressMonitor monitor) {
		File[] plugins = pluginsDir.listFiles();
		for (File file : plugins) {
			if (monitor.isCanceled())
				return;
			String nameAndVersion = file.getName();
			int underscoreIndex = nameAndVersion.indexOf('_');
			int dotIndex = nameAndVersion.lastIndexOf('.');
			String id = nameAndVersion.substring(0, underscoreIndex);
			String version = nameAndVersion.substring(underscoreIndex + 1,
					dotIndex);

			BundleDescriptor descriptor = new BundleDescriptor();
			descriptor.setBundleID(id);
			descriptor.setVersion(version);
			String appName = null;
			for (PiPlugBundle bundle : sourceBundles) {
				bundle.setDescriptor(descriptor);
				if (id.equals(bundle.getDescriptor().getBundleID())) {
					appName = bundle.getExtensions().first().getName();
					break;
				}
			}

			descriptor.setAppName(appName);

			try {
				client.uploadBundle(descriptor, file);
				status.add(new Status(IStatus.OK, Activator.PLUGIN_ID,
						"Deployed " + descriptor, null));
			} catch (CoreException e) {
				status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Failed to deploy " + descriptor, e));
			}
			monitor.worked(1);
		}
	}

	private void bucketSelectedExtensions(Set<PiPlugBundle> sourceBundles,
			Set<PiPlugBundle> binaryBundles) {
		for (PiPlugExtension app : extensions) {
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

	private FeatureExportInfo createExportInfo(Set<PiPlugBundle> sourceBundles)
			throws IOException {
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

	private Object[] toPluginModelArray(Set<PiPlugBundle> sourceBundles) {
		ArrayList<IPluginModelBase> result = new ArrayList<IPluginModelBase>();
		for (PiPlugBundle bundle : sourceBundles) {
			IPluginModelBase plugin = bundle.getPlugin();
			if (null != plugin) {
				result.add(plugin);
			}
		}
		return result.toArray();
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
