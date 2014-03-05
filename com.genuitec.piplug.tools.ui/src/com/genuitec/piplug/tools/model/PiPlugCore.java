package com.genuitec.piplug.tools.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

@SuppressWarnings("restriction")
public class PiPlugCore {
	public enum ListenerEventFlag {
		ADDED, CHANGED, REMOVED;
	}

	public class PiPlugPDEListener implements IPluginModelListener,
			IExtensionDeltaListener {

		private final class ExtensionsChangedSyncJob extends Job {
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
						PiPlugBundle bundle = bundles.remove(PiPlugBundleIdentity
								.fromPluginModelBase(plugin));
						fireBundleEvent(bundle, ListenerEventFlag.REMOVED);
					}
				}
				return Status.OK_STATUS;
			}
		}

		@Override
		public void modelsChanged(PluginModelDelta delta) {
			reload(delta.getAddedEntries(), ListenerEventFlag.ADDED);
			reload(delta.getChangedEntries(), ListenerEventFlag.CHANGED);

			// Do removals at the end, in case a change & removal is sent
			ModelEntry[] removed = delta.getRemovedEntries();
			if (null != removed && removed.length > 0) {
				for (ModelEntry entry : removed) {
					String pluginId = entry.getId();
					Set<PiPlugBundleIdentity> toRemove = new HashSet<PiPlugBundleIdentity>();
					synchronized (bundles) {
						for (PiPlugBundleIdentity next : bundles.keySet()) {
							if (pluginId.equals(next.getId())) {
								toRemove.add(next);
							}
						}
						for (PiPlugBundleIdentity bundleIdentity : toRemove) {
							PiPlugBundle bundle = bundles
									.remove(bundleIdentity);
							fireBundleEvent(bundle, ListenerEventFlag.REMOVED);
						}
					}
				}
			}
		}

		@Override
		public void extensionsChanged(final IExtensionDeltaEvent event) {
			new ExtensionsChangedSyncJob(event).schedule();
		}
	}

	private static final PiPlugCore instance = new PiPlugCore();
	private Map<PiPlugBundleIdentity, PiPlugBundle> bundles = new HashMap<PiPlugBundleIdentity, PiPlugBundle>();
	private Set<IPiPlugApplicationListener> listeners = new HashSet<IPiPlugApplicationListener>();

	public static PiPlugCore getInstance() {
		return instance;
	}

	public void addPiPlugApplicationListener(IPiPlugApplicationListener listener) {
		listeners.add(listener);
	}

	public void removePiPlugApplicationListener(
			IPiPlugApplicationListener listener) {
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

			bundles.put(PiPlugBundleIdentity.fromPluginModelBase(plugin),
					bundle);
			fireBundleEvent(bundle, listenerFlag);
		}
	}

	private PiPlugBundle createPiPlugBundle(IPluginModelBase plugin) {
		IExtensions pluginExtensions = plugin.getExtensions();
		if (null == pluginExtensions)
			return null;

		Set<PiPlugApplicationExtension> bundleApps = new HashSet<PiPlugApplicationExtension>();

		IPluginExtension[] extensions = pluginExtensions.getExtensions();
		if (null != extensions && extensions.length > 0) {
			for (IPluginExtension extension : extensions) {
				for (PiPlugExtensionType extensionType : PiPlugExtensionType
						.values()) {
					if (extensionType.getExtensionPointId().equals(
							extension.getPoint())) {
						bundleApps.addAll(getExtensions(extension,
								extensionType));
					}
				}
			}
		}

		// Shouldn't get this, but in any case don't create an inconsistent
		// model.
		if (bundleApps.isEmpty())
			return null;

		PiPlugBundle bundle = new PiPlugBundle(plugin.getBundleDescription()
				.getName());
		bundle.setApplications(bundleApps);

		return bundle;
	}

	private void fireBundleEvent(PiPlugBundle bundle,
			ListenerEventFlag listenerFlag) {
		if (null == bundle)
			return;
		if (listeners.isEmpty())
			return;
		for (IPiPlugApplicationListener listener : listeners) {
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

	private Set<PiPlugApplicationExtension> getExtensions(
			IPluginExtension extension, PiPlugExtensionType extensionType) {
		IPluginObject[] children = extension.getChildren();
		HashSet<PiPlugApplicationExtension> appExtensions = new HashSet<PiPlugApplicationExtension>();
		if (null == children || children.length == 0)
			return appExtensions;

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
				appExtensions.add(new PiPlugApplicationExtension(name, image,
						extensionType));
			}
		}

		return appExtensions;
	}

	private PiPlugCore() {
		// First, register listeners for future changes
		registerWorkspaceListener();
		registerClientListener();

		initialize();
	}

	private void registerClientListener() {
		// TODO Auto-generated method stub

	}

	private void registerWorkspaceListener() {
		PiPlugPDEListener listener = new PiPlugPDEListener();
		PDECore.getDefault().getModelManager().addPluginModelListener(listener);
		PDECore.getDefault().getModelManager()
				.addExtensionDeltaListener(listener);

	}

	private void initialize() {
		bundles = new HashMap<PiPlugBundleIdentity, PiPlugBundle>();

		PDEExtensionRegistry extensionsRegistry = PDECore.getDefault()
				.getExtensionsRegistry();

		// Find all plugins that have extensions of our extension points
		Set<IPluginModelBase> pluginModels = new HashSet<IPluginModelBase>();
		for (PiPlugExtensionType extensionType : PiPlugExtensionType.values()) {
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

			bundles.put(bundle.getIdentity(), bundle);
		}
	}

	public Set<PiPlugBundle> getBundles() {
		return new HashSet<PiPlugBundle>(bundles.values());
	}

	public static boolean isDebug() {
		return "true".equals(System.getProperty("piplug.debug"));
	}
}
