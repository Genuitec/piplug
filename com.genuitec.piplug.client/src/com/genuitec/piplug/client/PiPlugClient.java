package com.genuitec.piplug.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.genuitec.piplug.client.internal.DiscoverDaemonService;

public class PiPlugClient {

    private class ClientListeningJob extends Job {

	public ClientListeningJob() {
	    super("PiPlug Client Listener");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
	    if (null == connectedTo) {
		return Status.OK_STATUS;
	    }
	    try {
		waitForConnect(monitor);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    try {
		listenForEvents();
	    } catch (Exception e) {
		// timeout, just schedule again
		e.printStackTrace();
	    }
	    if (!monitor.isCanceled())
		schedule();
	    return Status.OK_STATUS;
	}

	private void waitForConnect(IProgressMonitor monitor)
		throws InterruptedException {
	    while (null == connectedTo && !monitor.isCanceled()) {
		synchronized (PiPlugClient.this) {
		    if (null == connectedTo)
			wait(1000);
		}
	    }
	}

    }

    private static final String ID = "com.genuitec.piplug.client";
    private InetSocketAddress connectedTo;
    private JAXBContext jaxb;
    private Set<IPiPlugClientListener> listeners = new HashSet<IPiPlugClientListener>();
    private ClientListeningJob listeningJob;

    public PiPlugClient() {
	try {
	    jaxb = JAXBContext.newInstance(BundleDescriptors.class,
		    BundleEvents.class);
	} catch (Exception e) {
	    throw new IllegalStateException(
		    "Unable to prepare JAXB context for serialization");
	}
    }

    protected void listenForEvents() throws CoreException {
	checkConnected();
	try {
	    URL url = urlTo("/listen");
	    HttpURLConnection connection = (HttpURLConnection) url
		    .openConnection();
	    connection.setReadTimeout(5 * 60 * 60 * 1000);
	    Reader raw = new InputStreamReader(connection.getInputStream(),
		    "UTF-8");
	    try {
		Unmarshaller unmarshaller = jaxb.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new BufferedReader(raw));
		List<BundleEvent> events = ((BundleEvents) result).getEvents();
		fireEvents(events);
	    } finally {
		raw.close();
	    }
	} catch (Exception ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to listen for events", ioe);
	    throw new CoreException(status);
	}
    }

    private void fireEvents(List<BundleEvent> events) {
	if (listeners.isEmpty())
	    return;
	for (BundleEvent event : events) {
	    for (IPiPlugClientListener listener : listeners) {
		switch (event.getType()) {
		case ADDED:
		    listener.bundleAdded(event.getDescriptor());
		    break;
		case CHANGED:
		    listener.bundleChanged(event.getDescriptor());
		    break;
		case REMOVED:
		    listener.bundleRemoved(event.getDescriptor());
		    break;
		}
	    }
	}
    }

    public InetSocketAddress discoverServer(int timeoutInMillis)
	    throws CoreException {
	return DiscoverDaemonService.discover(timeoutInMillis);
    }

    public void connectTo(InetSocketAddress address) throws CoreException {
	try {
	    Reader raw = new InputStreamReader(urlTo(address, "/connect")
		    .openStream(), "UTF-8");
	    try {
		String response = new BufferedReader(raw).readLine();
		if ("connection-alive".equals(response)) {
		    connectedTo = address;
		} else {
		    IStatus status = new Status(IStatus.ERROR, ID,
			    "Unable to connect to " + address
				    + ": unexpected response [" + response
				    + "]");
		    throw new CoreException(status);
		}
	    } finally {
		raw.close();
	    }
	} catch (IOException ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to connect to " + address, ioe);
	    throw new CoreException(status);
	}
    }

    public void disconnect() throws CoreException {
	// TODO clean up listener thread
	connectedTo = null;
    }

    public List<BundleDescriptor> listBundles() throws CoreException {
	checkConnected();
	try {
	    Reader raw = new InputStreamReader(urlTo("/list-bundles")
		    .openStream(), "UTF-8");
	    try {
		Unmarshaller unmarshaller = jaxb.createUnmarshaller();
		Object result = unmarshaller.unmarshal(new BufferedReader(raw));
		return ((BundleDescriptors) result).getDescriptors();
	    } finally {
		raw.close();
	    }
	} catch (Exception ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to retrieve bundle descriptors", ioe);
	    throw new CoreException(status);
	}
    }

    public void downloadBundle(BundleDescriptor descriptor, File targetFile)
	    throws CoreException {
	checkConnected();
	try {
	    URL url = urlTo("/get-bundle");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestProperty("Bundle-ID", descriptor.getBundleID());
	    conn.setRequestProperty("Bundle-Version", descriptor.getVersion()
		    .toString());
	    conn.connect();
	    int code = conn.getResponseCode();
	    if (code != 200) {
		IStatus status = new Status(IStatus.ERROR, ID, code,
			"Unable to retrieve bundle [" + code + "]", null);
		throw new CoreException(status);
	    }
	    File dir = targetFile.getParentFile();
	    if (dir != null)
		dir.mkdirs();
	    File newFile = new File(targetFile.getParentFile(),
		    targetFile.getName() + ".new");
	    byte buffer[] = new byte[1024 * 1024];
	    boolean cleanup = true;
	    FileOutputStream out = new FileOutputStream(newFile);
	    try {
		int numread;
		InputStream in = conn.getInputStream();
		while (0 <= (numread = in.read(buffer)))
		    out.write(buffer, 0, numread);
		cleanup = false;
	    } finally {
		out.close();
		if (cleanup)
		    newFile.delete();
	    }
	    // move the file into place
	    targetFile.delete();
	    newFile.renameTo(targetFile);
	} catch (Exception ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to retrieve bundle contents", ioe);
	    throw new CoreException(status);
	}
    }

    public void uploadBundle(BundleDescriptor descriptor, File sourceFile)
	    throws CoreException {
	checkConnected();
	try {
	    InputStream in = new FileInputStream(sourceFile);
	    try {
		URL url = urlTo("/put-bundle");
		HttpURLConnection conn = (HttpURLConnection) url
			.openConnection();
		conn.setRequestProperty("Bundle-ID", descriptor.getBundleID());
		conn.setRequestProperty("Bundle-Version", descriptor
			.getVersion().toString());
		conn.setRequestProperty("Content-Length",
			String.valueOf(sourceFile.length()));
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);
		conn.connect();
		OutputStream out = conn.getOutputStream();
		byte buffer[] = new byte[1024 * 1024];
		try {
		    int numread;
		    while (0 <= (numread = in.read(buffer)))
			out.write(buffer, 0, numread);
		} finally {
		    out.flush();
		}

		int code = conn.getResponseCode();
		if (code != 200) {
		    IStatus status = new Status(IStatus.ERROR, ID, code,
			    "Unable to upload bundle [" + code + "]", null);
		    throw new CoreException(status);
		}
	    } finally {
		in.close();
	    }
	} catch (Exception ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to upload bundle contents", ioe);
	    throw new CoreException(status);
	}
    }

    public void removeBundle(BundleDescriptor descriptor) throws CoreException {
	checkConnected();
	try {
	    URL url = urlTo("/remove-bundle");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestProperty("Bundle-ID", descriptor.getBundleID());
	    conn.setRequestProperty("Bundle-Version", descriptor.getVersion()
		    .toString());
	    conn.connect();
	    try {
		int code = conn.getResponseCode();
		if (code != 200) {
		    IStatus status = new Status(IStatus.ERROR, ID, code,
			    "Unable to remove bundle [" + code + "]", null);
		    throw new CoreException(status);
		}
	    } finally {
		conn.disconnect();
	    }
	} catch (Exception ioe) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "Unable to remove bundle " + descriptor.getBundleID(), ioe);
	    throw new CoreException(status);
	}
    }

    public synchronized void addListener(IPiPlugClientListener listener) {
	listeners.add(listener);
	if (listeningJob == null) {
	    listeningJob = new ClientListeningJob();
	    listeningJob.schedule();
	}
    }

    public synchronized void removeListener(IPiPlugClientListener listener) {
	listeners.remove(listener);
	if (listeners.isEmpty()) {
	    listeningJob.cancel();
	    listeningJob = null;
	}
    }

    private void checkConnected() throws CoreException {
	if (connectedTo == null) {
	    IStatus status = new Status(IStatus.ERROR, ID,
		    "You must call connectTo(...) before using this API");
	    throw new CoreException(status);
	}
    }

    private URL urlTo(InetSocketAddress address, String path)
	    throws MalformedURLException {
	return new URL("http", address.getHostName(), address.getPort(), path);
    }

    private URL urlTo(String path) throws MalformedURLException, CoreException {
	checkConnected();
	return new URL("http", connectedTo.getHostName(),
		connectedTo.getPort(), path);
    }
}
