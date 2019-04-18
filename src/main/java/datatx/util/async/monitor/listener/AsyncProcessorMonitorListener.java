package datatx.util.async.monitor.listener;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.RegionEvent;

import datatx.util.async.monitor.processor.AsyncProcessorMonitor;

@SuppressWarnings("rawtypes")
public class AsyncProcessorMonitorListener implements CacheListener {

	@Override
	public void afterCreate(EntryEvent event) {
		// Don't care		
	}

	@Override
	public void afterUpdate(EntryEvent event) {
		// Don't care		
	}

	@Override
	public void afterInvalidate(EntryEvent event) {
		// Don't care		
	}

	@Override
	public void afterDestroy(EntryEvent event) {
		// Don't care		
	}

	@Override
	public void afterRegionInvalidate(RegionEvent event) {
		// Don't care		
	}

	@Override
	public void afterRegionDestroy(RegionEvent event) {
		AsyncProcessorMonitor.getInstance().shutdown();
	}

	@Override
	public void afterRegionClear(RegionEvent event) {
		// Don't care		
	}

	@Override
	public void afterRegionCreate(RegionEvent event) {
		// Don't care		
	}

	@Override
	public void afterRegionLive(RegionEvent event) {
		// Don't care		
	}
}
