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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.PluginExportOperation;
import org.osgi.framework.Version;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.PiPlugApplicationExtension;
import com.genuitec.piplug.tools.model.PiPlugBundle;

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
		sortSelectedApps(sourceBundles, binaryBundles);
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
					// if (logLocation.exists()) {
					// PlatformUI.getWorkbench().getDisplay()
					// .syncExec(new Runnable() {
					// public void run() {
					// AntErrorDialog dialog = new AntErrorDialog(
					// logLocation);
					// dialog.open();
					// }
					// });
					// }
					System.out.println("Export problem, see " + logLocation);
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
		try {
			InetSocketAddress serverAddress = client.discoverServer(30000);
			client.connectTo(serverAddress);
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}

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
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Binary plugin deploy
	}

	private void sortSelectedApps(SortedSet<PiPlugBundle> sourceBundles,
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
		info.items = toIModelArray(sourceBundles);
		info.signingInfo = null;
		info.qualifier = null;

		return info;
	}

	private Object[] toIModelArray(SortedSet<PiPlugBundle> bundles) {
		ArrayList<IModel> result = new ArrayList<IModel>();
		for (PiPlugBundle bundle : bundles) {
			IPluginModelBase plugin = bundle.getPlugin();
			if (null != plugin) {
				result.add(plugin);
			}
		}
		return result.toArray();
	}
}
