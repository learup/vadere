package org.vadere.simulator.utils.cache;

import org.vadere.util.data.cellgrid.CellGrid;

public interface CacheLoader {

	void loadCacheFor(CellGrid cellGrid) throws CacheException;

}
