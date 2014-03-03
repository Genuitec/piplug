package com.genuitec.piplug.tools.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.IExtensionDeltaEvent;
import org.eclipse.pde.internal.core.IExtensionDeltaListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEExtensionRegistry;

@SuppressWarnings("restriction")
public class PiPlugCore {
	public enum ListenerEventFlag {
		ADDED, CHANGED, REMOVED;
	}

	public class ExtensionDeltaListener implements IExtensionDeltaListener {

		@Override
		public void extensionsChanged(IExtensionDeltaEvent event) {
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
					fireBundleChanged(bundle, ListenerEventFlag.REMOVED);
				}
			}

		}
	}

	private static final String PIPLUG_APP_EXTENSION_POINT = "com.genuitec.piplug.api.app";
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

	protected void reload(IPluginModelBase[] added,
			ListenerEventFlag listenerFlag) {
		if (null == added || added.length == 0)
			return;

		for (IPluginModelBase plugin : added) {
			PiPlugBundle bundle = createPiPlugBundle(plugin);
			if (null == bundle)
				continue;
			
			bundles.put(PiPlugBundleIdentity.fromPluginModelBase(plugin),
					bundle);
			fireBundleChanged(bundle, listenerFlag);
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
				bundleApps.addAll(getAppExtensions(extension));
			}
		}
		PiPlugBundle bundle = new PiPlugBundle(plugin
				.getBundleDescription().getName());
		bundle.setApplications(bundleApps);
		return bundle;
	}

	private void fireBundleChanged(PiPlugBundle bundle,
			ListenerEventFlag listenerFlag) {
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

	private Set<PiPlugApplicationExtension> getAppExtensions(
			IPluginExtension extension) {
		IPluginObject[] children = extension.getChildren();
		HashSet<PiPlugApplicationExtension> appExtensions = new HashSet<PiPlugApplicationExtension>();
		if (null == children || children.length == 0)
			return appExtensions;

		// We're matching
		// <extension
		// point="com.genuitec.piplug.api.app">
		// <piplug-app
		// class="com.genuitec.piplug.app.snake.SnakeApplication"
		// image="app.png"
		// name="Snake">
		// </piplug-app>
		// </extension>

		for (IPluginObject pluginObject : children) {
			if (pluginObject instanceof IPluginElement) {
				IPluginElement element = (IPluginElement) pluginObject;
				IPluginAttribute nameAttribute = element.getAttribute("name");
				String name = nameAttribute == null ? "Error: app name required in extension" : nameAttribute.getValue();
				IPluginAttribute imageAttribute = element.getAttribute("image");
				if (imageAttribute == null) {
					// old-school
					imageAttribute = element.getAttribute("img64");
				}
				String image = imageAttribute == null ? null : imageAttribute.getValue();
				appExtensions.add(new PiPlugApplicationExtension(name, image));
			}
		}

		return appExtensions;
	}

	private PiPlugCore() {
		initialize();
	}

	private void initialize() {
		bundles = new HashMap<PiPlugBundleIdentity, PiPlugBundle>();
		PDEExtensionRegistry extensionsRegistry = PDECore.getDefault()
				.getExtensionsRegistry();

		// First, register a listener for future changes
		PDECore.getDefault().getModelManager()
				.addExtensionDeltaListener(new ExtensionDeltaListener());

		// Second, read the existing extensions
		IPluginModelBase[] plugins = extensionsRegistry.findExtensionPlugins(
				PIPLUG_APP_EXTENSION_POINT, true);
		if (null != plugins) {
			for (IPluginModelBase plugin : plugins) {
				PiPlugBundle bundle = createPiPlugBundle(plugin);
				if (null == bundle)
					continue;

				bundles.put(bundle.getIdentity(), bundle);
			}
		}
	}


	public Set<PiPlugBundle> getBundles() {
		return new HashSet<PiPlugBundle>(bundles.values());
	}
}
