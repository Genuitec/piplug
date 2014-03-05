package com.genuitec.piplug.tools.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginBase;
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
import org.osgi.framework.Version;

import com.genuitec.piplug.client.BundleDescriptor;
import com.genuitec.piplug.client.PiPlugClient;
import com.genuitec.piplug.tools.model.internal.ClientSyncJob;

@SuppressWarnings("restriction")
public class PiPlugCore {
	public enum ListenerEventFlag {
		ADDED, CHANGED, REMOVED;
	}

	public class PiPlugPDEListener implements IPluginModelListener,
			IExtensionDeltaListener {

		@Override
		public void modelsChanged(PluginModelDelta delta) {
			reload(delta.getAddedEntries(), ListenerEventFlag.ADDED);
			reload(delta.getChangedEntries(), ListenerEventFlag.CHANGED);

			// Do removals at the end, in case a change & removal is sent
			ModelEntry[] removed = delta.getRemovedEntries();
			if (null != removed && removed.length > 0) {
				for (ModelEntry entry : removed) {
					String pluginId = entry.getId();
					Set<BundleDescriptor> toRemove = new HashSet<BundleDescriptor>();
					synchronized (localBundles) {
						for (BundleDescriptor next : localBundles.keySet()) {
							if (pluginId.equals(next.getBundleID())) {
								toRemove.add(next);
							}
						}
						for (BundleDescriptor bundleIdentity : toRemove) {
							PiPlugBundle bundle = localBundles
									.remove(bundleIdentity);
							fireBundleEvent(bundle, ListenerEventFlag.REMOVED);
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
				reload(added, ListenerEventFlag.ADDED);

			IPluginModelBase[] changed = event.getChangedModels();
			if (null != changed && changed.length > 0)
				reload(changed, ListenerEventFlag.CHANGED);

			// Do removals at the end, in case a change & removal is sent
			IPluginModelBase[] removed = event.getRemovedModels();
			if (null != removed && removed.length > 0) {
				for (IPluginModelBase plugin : removed) {
					PiPlugBundle bundle = localBundles.remove(PiPlugCore
							.fromPluginModelBase(plugin));
					fireBundleEvent(bundle, ListenerEventFlag.REMOVED);
				}
			}
			return Status.OK_STATUS;
		}
	}

	private static final PiPlugCore instance = new PiPlugCore();
	private Map<BundleDescriptor, PiPlugBundle> localBundles = new HashMap<BundleDescriptor, PiPlugBundle>();
	private List<BundleDescriptor> remoteBundles;
	private Set<IPiPlugBundleListener> listeners = new HashSet<IPiPlugBundleListener>();
	private PiPlugClient client;
	
	public static PiPlugCore getInstance() {
		return instance;
	}

	public void addPiPlugApplicationListener(IPiPlugBundleListener listener) {
		listeners.add(listener);
	}

	public void removePiPlugApplicationListener(
			IPiPlugBundleListener listener) {
		listeners.remove(listener);
	}

	protected void reload(ModelEntry[] modelEntries,
			ListenerEventFlag listenerFlag) {
		if (null == modelEntries || modelEntries.length == 0)
			return;

		for (ModelEntry entry : modelEntries) {
			reload(entry.getActiveModels(), listenerFlag);
		}
	}

	private void reload(IPluginModelBase[] plugins,
			ListenerEventFlag listenerFlag) {
		if (null == plugins)
			return;
		for (IPluginModelBase plugin : plugins) {
			PiPlugBundle bundle = createPiPlugBundle(plugin);
			if (null == bundle)
				continue;

			localBundles.put(PiPlugCore.fromPluginModelBase(plugin),
					bundle);
			fireBundleEvent(bundle, listenerFlag);
		}
	}

	private PiPlugBundle createPiPlugBundle(IPluginModelBase plugin) {
		IExtensions pluginExtensions = plugin.getExtensions();
		if (null == pluginExtensions)
			return null;

		Set<PiPlugExtension> bundleExtensions = new HashSet<PiPlugExtension>();

		IPluginExtension[] extensions = pluginExtensions.getExtensions();
		if (null != extensions && extensions.length > 0) {
			for (IPluginExtension extension : extensions) {
				for (ExtensionType extensionType : ExtensionType
						.values()) {
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

	private void fireBundleEvent(PiPlugBundle bundle,
			ListenerEventFlag listenerFlag) {
		if (null == bundle)
			return;
		if (listeners.isEmpty())
			return;
		for (IPiPlugBundleListener listener : listeners) {
			switch (listenerFlag) {
			case ADDED:
				listener.bundleAdded(bundle);
				break;
			case CHANGED:
				listener.bundleChanged(bundle);
				break;
			case REMOVED:
				listener.bundleRemoved(bundle);
				break;
			}
		}
	}

	private Set<PiPlugExtension> getExtensions(
			IPluginExtension extension, ExtensionType extensionType) {
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
				extensions.add(new PiPlugExtension(name, image,
						extensionType));
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
		new ClientSyncJob(client).schedule();
	}

	private void registerWorkspaceListener() {
		PiPlugPDEListener listener = new PiPlugPDEListener();
		PDECore.getDefault().getModelManager().addPluginModelListener(listener);
		PDECore.getDefault().getModelManager()
				.addExtensionDeltaListener(listener);

	}

	private void initialize() {
		localBundles = new HashMap<BundleDescriptor, PiPlugBundle>();

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

			localBundles.put(bundle.getDescriptor(), bundle);
		}
	}

	public Set<PiPlugBundle> getLocalBundles() {
		return new HashSet<PiPlugBundle>(localBundles.values());
	}

	public static BundleDescriptor fromPluginModelBase(IPluginBase plugin) {
		return new BundleDescriptor(plugin.getId(), Version.parseVersion(plugin.getVersion()), null, null);
	}

	public static BundleDescriptor fromPluginModelBase(IPluginModelBase plugin) {
		BundleDescription bundleDescription = plugin.getBundleDescription();
		return new BundleDescriptor(bundleDescription.getName(), bundleDescription.getVersion(), null, null);
	}

	public static boolean isDebug() {
		return "true".equals(System.getProperty("piplug.debug"));
	}
	
	public void setRemoteBundles(List<BundleDescriptor> bundles) {
		this.remoteBundles = bundles;
		assignNewDescriptors();
	}

	private void assignNewDescriptors() {
		if (remoteBundles == null) {
			// Null out dates in old descriptors
		} else {
			Map<BundleDescriptor, BundleDescriptor> toReHash = new HashMap<BundleDescriptor, BundleDescriptor>();
			for (BundleDescriptor localDescriptor : localBundles.keySet()) {
				for (BundleDescriptor remoteDescriptor : remoteBundles) {
					if (localDescriptor.matchesID(remoteDescriptor)) {
						toReHash.put(localDescriptor, remoteDescriptor);
					}
				}
			}
			
			for (Entry<BundleDescriptor, BundleDescriptor> next : toReHash.entrySet()) {
				BundleDescriptor localDescriptor = next.getKey();
				PiPlugBundle bundle = localBundles.remove(localDescriptor);
				BundleDescriptor remoteDescriptor = next.getValue();
				bundle.setDescriptor(remoteDescriptor);
				localBundles.put(remoteDescriptor, bundle);
				fireBundleEvent(bundle, ListenerEventFlag.CHANGED);
			}
		}
	}
}
