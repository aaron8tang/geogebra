package org.geogebra.common.kernel.geos;

import java.util.ArrayList;

import org.geogebra.common.kernel.Kernel;

public class ListenerList {
	private ArrayList<GeoElement> listeners;
	private Kernel kernel;

	public ListenerList(Kernel kernel) {
		this.kernel = kernel;
		this.listeners = new ArrayList<>();
	}

	/**
	 * Registers geo as a listener.
	 *
	 * @param geo
	 *            geo as the listener
	 */
	public void register(GeoElement geo) {
		listeners.add(geo);
	}

	/**
	 * Unregisters geo as a listener.
	 *
	 * @param geo
	 *            geo as a listener
	 */
	public void unregister(GeoElement geo) {
		listeners.remove(geo);
	}

	/**
	 * Remove all listeners
	 */
	public void clear() {
		listeners.clear();
	}

	public void notifyUpdate() {
		for (GeoElement geo: listeners) {
			kernel.notifyUpdate(geo);
		}
	}
}
