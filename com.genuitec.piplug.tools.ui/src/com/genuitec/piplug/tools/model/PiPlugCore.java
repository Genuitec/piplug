package com.genuitec.piplug.tools.model;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.internal.core.IExtensionDeltaEvent;
import org.eclipse.pde.internal.core.IExtensionDeltaListener;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEExtensionRegistry;
import org.eclipse.pde.internal.core.PluginModelDelta;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.BundleDescriptors;
import com.genuitec.piplug.client.IPiPlugClientListener;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.daemon.PiPlugDaemon;
import com.genuitec.piplug.tools.ui.Activator;

@SuppressWarnings("restriction")
public class PiPlugCore implements IPiPlugClientListener {
	public class PiPlugPDEListener implements IPluginModelListener,
			IExtensionDeltaListener {

		@Override
		public void modelsChanged(PluginModelDelta delta) {
			reload(delta.getAddedEntries());
			reload(delta.getChangedEntries());

			// Do removals at the end, in case a change & removal is sent
			ModelEntry[] removed = delta.getRemovedEntries();
			if (null != removed && removed.length > 0) {
				for (ModelEntry entry : removed) {
					String pluginId = entry.getId();
					Set<BundleDescriptor> toRemove = new HashSet<BundleDescriptor>();
					synchronized (localBundleDescriptors) {
						List<BundleDescriptor> descriptors = localBundleDescriptors
								.getDescriptors();
						for (BundleDescriptor next : descriptors) {
							if (pluginId.equals(next.getBundleID())) {
								toRemove.add(next);
							}
						}
						if (!toRemove.isEmpty()) {
							localBundleDescriptors.getDescriptors().removeAll(
									descriptors);
							fireBundleDescriptorsChanged();
						}
					}
				}
			}
		}

		@Override
		public void extensionsChanged(final IExtensionDeltaEvent event) {
			// PDE returns the old model during this callback, so react
			// out of band
			new ExtensionsChangedSyncJob(event).schedule();
		}
	}

	private class ExtensionsChangedSyncJob extends Job {
		private final IExtensionDeltaEvent event;

		private ExtensionsChangedSyncJob(IExtensionDeltaEvent event) {
			super("Extensions changed model synchronization");
			setUser(false);
			setSystem(true);
			this.event = event;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IPluginModelBase[] added = event.getAddedModels();
			if (null != added && added.length > 0)
				reload(added);

			IPluginModelBase[] changed = event.getChangedModels();
			if (null != changed && changed.length > 0)
				reload(changed);

			// Do removals at the end, in case a change & removal is sent
			IPluginModelBase[] removed = event.getRemovedModels();
			if (null != removed && removed.length > 0) {
				boolean didRemoval = false;
				for (IPluginModelBase plugin : removed) {
					BundleDescriptor descriptor = PiPlugCore
							.fromPluginModelBase(plugin);
					localBundleDescriptors.getDescriptors().remove(descriptor);
					didRemoval = true;
				}
				if (didRemoval)
					fireBundleDescriptorsChanged();
			}
			return Status.OK_STATUS;
		}
	}

	private static final int DISCOVER_TIMEOUT = 30000;

	private static final PiPlugCore instance = new PiPlugCore();

	private static final String KEY_BUNDLE = "key.bundle";
	private BundleDescriptors localBundleDescriptors;
	private BundleDescriptors remoteBundleDescriptors;
	private Set<IPiPlugBundleListener> listeners = new HashSet<IPiPlugBundleListener>();
	private PiPlugClient client;
	private PiPlugDaemon localDaemon;
	private CoreException daemonException;

	public static PiPlugCore getInstance() {
		return instance;
	}

	public void addPiPlugApplicationListener(IPiPlugBundleListener listener) {
		listeners.add(listener);
	}

	public void removePiPlugApplicationListener(IPiPlugBundleListener listener) {
		listeners.remove(listener);
	}

	protected void reload(ModelEntry[] modelEntries) {
		if (null == modelEntries || modelEntries.length == 0)
			return;

		for (ModelEntry entry : modelEntries) {
			reload(entry.getActiveModels());
		}
	}

	private void reload(IPluginModelBase[] plugins) {
		if (null == plugins)
			return;
		boolean changed = false;
		for (IPluginModelBase plugin : plugins) {
			PiPlugBundle bundle = createPiPlugBundle(plugin);
			if (null == bundle)
				continue;

			BundleDescriptor descriptor = PiPlugCore
					.fromPluginModelBase(plugin);
			descriptor.putData(KEY_BUNDLE, bundle);
			localBundleDescriptors.getDescriptors().add(descriptor);
			changed = true;
		}
		if (changed)
			fireBundleDescriptorsChanged();
	}

	private PiPlugBundle createPiPlugBundle(IPluginModelBase plugin) {
		IExtensions pluginExtensions = plugin.getExtensions();
		if (null == pluginExtensions)
			return null;

		Set<PiPlugExtension> bundleExtensions = new HashSet<PiPlugExtension>();

		IPluginExtension[] extensions = pluginExtensions.getExtensions();
		if (null != extensions && extensions.length > 0) {
			for (IPluginExtension extension : extensions) {
				for (ExtensionType extensionType : ExtensionType.values()) {
					if (extensionType.getExtensionPointId().equals(
							extension.getPoint())) {
						bundleExtensions.addAll(getExtensions(extension,
								extensionType));
					}
				}
			}
		}

		// Shouldn't get this, but in any case don't create an inconsistent
		// model.
		if (bundleExtensions.isEmpty())
			return null;

		PiPlugBundle bundle = new PiPlugBundle(plugin.getBundleDescription()
				.getName());
		bundle.setApplications(bundleExtensions);

		return bundle;
	}

	private void fireBundleDescriptorsChanged() {
		if (listeners.isEmpty())
			return;
		for (IPiPlugBundleListener listener : listeners) {
			listener.bundlesChanged(localBundleDescriptors,
					remoteBundleDescriptors);
		}
	}

	private Set<PiPlugExtension> getExtensions(IPluginExtension extension,
			ExtensionType extensionType) {
		IPluginObject[] children = extension.getChildren();
		HashSet<PiPlugExtension> extensions = new HashSet<PiPlugExtension>();
		if (null == children || children.length == 0)
			return extensions;

		// We're matching
		//
		// <extension
		// point="com.genuitec.piplug.api.app">
		// <piplug-app
		// class="com.genuitec.piplug.app.snake.SnakeApplication"
		// image="app.png"
		// name="Snake">
		// </piplug-app>
		// </extension>
		//
		// or
		//
		// <extension
		// point="com.genuitec.piplug.api.service">
		// <piplug-service
		// name="Infocom Emulator Service">
		// </piplug-service>
		// </extension>

		for (IPluginObject pluginObject : children) {
			if (pluginObject instanceof IPluginElement) {
				IPluginElement element = (IPluginElement) pluginObject;
				IPluginAttribute nameAttribute = element.getAttribute("name");
				String name = nameAttribute == null ? "Error: app name required in extension"
						: nameAttribute.getValue();
				IPluginAttribute imageAttribute = element.getAttribute("image");
				String image = imageAttribute == null ? null : imageAttribute
						.getValue();
				extensions.add(new PiPlugExtension(name, image, extensionType));
			}
		}

		return extensions;
	}

	private PiPlugCore() {
		// First, register listeners for future changes
		registerWorkspaceListener();
		registerClientListener();

		initialize();
	}

	private void registerClientListener() {
		client = new PiPlugClient();
		client.addListener(this);
	}

	private void registerWorkspaceListener() {
		PiPlugPDEListener listener = new PiPlugPDEListener();
		PDECore.getDefault().getModelManager().addPluginModelListener(listener);
		PDECore.getDefault().getModelManager()
				.addExtensionDeltaListener(listener);

	}

	private void initialize() {
		localBundleDescriptors = new BundleDescriptors();

		PDEExtensionRegistry extensionsRegistry = PDECore.getDefault()
				.getExtensionsRegistry();

		// Find all plugins that have extensions of our extension points
		Set<IPluginModelBase> pluginModels = new HashSet<IPluginModelBase>();
		for (ExtensionType extensionType : ExtensionType.values()) {
			IPluginModelBase[] plugins = extensionsRegistry
					.findExtensionPlugins(extensionType.getExtensionPointId(),
							true);
			if (null != plugins) {
				pluginModels.addAll(Arrays.asList(plugins));
			}
		}

		for (IPluginModelBase plugin : pluginModels) {
			PiPlugBundle bundle = createPiPlugBundle(plugin);
			if (null == bundle)
				continue;

			BundleDescriptor descriptor = bundle.getDescriptor();
			descriptor.putData(KEY_BUNDLE, bundle);
			localBundleDescriptors.getDescriptors().add(descriptor);
		}
	}

	public BundleDescriptors getLocalBundleDescriptors() {
		synchronized (localBundleDescriptors) {
			return (BundleDescriptors) localBundleDescriptors.clone();
		}
	}

	public static BundleDescriptor fromPluginModelBase(IPluginModelBase plugin) {
		BundleDescription bundleDescription = plugin.getBundleDescription();
		BundleDescriptor descriptor = new BundleDescriptor();
		descriptor.setBundleID(bundleDescription.getName());
		descriptor.setVersion(bundleDescription.getVersion().toString());
		return descriptor;
	}

	public static boolean isDebug() {
		return "true".equals(System.getProperty("piplug.debug"));
	}

	public void setRemoteBundleDescriptors(BundleDescriptors bundles) {
		this.remoteBundleDescriptors = bundles;
		fireBundleDescriptorsChanged();
	}

	// private void assignNewDescriptors() {
	// Map<BundleDescriptor, BundleDescriptor> toReHash = new
	// HashMap<BundleDescriptor, BundleDescriptor>();
	// Set<BundleDescriptor> theUndeployed = new HashSet<BundleDescriptor>(
	// localBundles.keySet());
	// for (BundleDescriptor localDescriptor : localBundles.keySet()) {
	// for (BundleDescriptor remoteDescriptor : remoteBundleDescriptors
	// .getDescriptors()) {
	// if (localDescriptor.matchesID(remoteDescriptor)) {
	// toReHash.put(localDescriptor, remoteDescriptor);
	// theUndeployed.remove(localDescriptor);
	// }
	// }
	// }
	//
	// for (BundleDescriptor bundleDescriptor : theUndeployed) {
	// bundleDescriptor.setFirstAdded(null);
	// bundleDescriptor.setLastUpdatedOn(null);
	// fireBundleDescriptorsChanged(localBundles.get(bundleDescriptor),
	// ListenerEventFlag.CHANGED);
	// }
	//
	// for (Entry<BundleDescriptor, BundleDescriptor> next : toReHash
	// .entrySet()) {
	// BundleDescriptor localDescriptor = next.getKey();
	// PiPlugBundle bundle = localBundles.remove(localDescriptor);
	// BundleDescriptor remoteDescriptor = next.getValue();
	// bundle.setDescriptor(remoteDescriptor);
	// localBundles.put(remoteDescriptor, bundle);
	// fireBundleDescriptorsChanged(bundle, ListenerEventFlag.CHANGED);
	// }
	// }

	public synchronized void startDaemonLocally() {
		// It's already running but we're having communications issues.
		// Let's just stop the old one and start a new one.
		if (daemonException != null) {
			daemonException = null;
		}
		if (localDaemon != null) {
			localDaemon.stop();
			localDaemon = null;
		}
		IPath stateLocation = Activator.getDefault().getStateLocation();
		IPath storage = stateLocation.append("daemon-storage");
		File storageLocation = storage.toFile();
		storageLocation.mkdirs();
		boolean debug = "true".equals(System.getProperty("daemon.debug"));
		PiPlugDaemon daemon = new PiPlugDaemon(storageLocation, debug);
		try {
			daemon.start();
			synchronized (this) {
				this.localDaemon = daemon;
				notifyAll();
			}
		} catch (IOException e) {
			synchronized (this) {
				daemonException = new CoreException(new Status(IStatus.OK,
						Activator.PLUGIN_ID, "Could not start a local daemon",
						e));
				notifyAll();
			}
			Activator.getDefault().getLog().log(daemonException.getStatus());
		}
	}

	public void waitForDaemon() throws CoreException {
		if (client.isConnected())
			return;
		if (daemonException != null)
			throw daemonException;
		synchronized (this) {
			while (!client.isConnected() && daemonException == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		if (daemonException != null)
			throw daemonException;
	}

	public void scheduleFindDaemon(IDaemonStateListener listener) {
		new FindDaemonJob(listener).schedule();
	}

	public boolean hasDaemonConnection() {
		return client.isConnected();
	}

	public InetSocketAddress tryToDiscoverDaemon(boolean logError) {
		try {
			return client.discoverServer(DISCOVER_TIMEOUT);
		} catch (CoreException e) {
			if (logError)
				Activator
						.getDefault()
						.getLog()
						.log(new Status(
								IStatus.ERROR,
								Activator.PLUGIN_ID,
								"Could not discover running PiPlug daemon process",
								e));

			return null;
		}
	}

	public PiPlugClient getClient() {
		return client;
	}

	@Override
	public void newBundleList(BundleDescriptors descriptors) {
		setRemoteBundleDescriptors(descriptors);
	}

	public PiPlugBundle getBundle(BundleDescriptor next) {
		return (PiPlugBundle) next.getData(KEY_BUNDLE);
	}

	public BundleDescriptor getRemoteBundleDescriptor(
			BundleDescriptor localBundleDescriptor) {
		if (null == localBundleDescriptor)
			return null;
		if (null == remoteBundleDescriptors)
			return null;
		List<BundleDescriptor> descriptors = remoteBundleDescriptors
				.getDescriptors();
		if (null == descriptors)
			return null;
		for (BundleDescriptor next : descriptors) {
			if (localBundleDescriptor.matchesID(next))
				return next;
		}
		return null;
	}

	public void shutdown() {
		if (null != client) {
			try {
				client.disconnect();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			client = null;
		}
	}
}
