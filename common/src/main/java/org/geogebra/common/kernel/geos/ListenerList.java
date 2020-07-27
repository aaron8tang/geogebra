package org.geogebra.common.kernel.geos;

import java.util.ArrayList;
import java.util.Iterator;

import org.geogebra.common.kernel.Kernel;

public class ListenerList implements Iterable<GeoElement> {
	private ArrayList<GeoElement> geos;
	private Kernel kernel;

	public ListenerList(Kernel kernel) {
		this.kernel = kernel;
		this.geos = new ArrayList<>();
	}

	/**
	 * Registers geo as a listener.
	 *
	 * @param geo
	 *            geo as the listener
	 */
	public void register(GeoElement geo) {
		geos.add(geo);
	}

	/**
	 * Unregisters geo as a listener.
	 *
	 * @param geo
	 *            geo as a listener
	 */
	public void unregister(GeoElement geo) {
		geos.remove(geo);
	}

	/**
	 * Remove all listeners
	 */
	public void clear() {
		geos.clear();
	}

	public void notifyUpdate() {
		for (GeoElement geo: geos) {
			kernel.notifyUpdate(geo);
		}
	}

	@Override
	public Iterator<GeoElement> iterator() {
		return geos.iterator();
	}
}
