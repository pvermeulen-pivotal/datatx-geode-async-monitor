package datatx.util.async.function.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.RegionFunctionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatx.util.async.monitor.processor.AsyncProcessorMonitor;

public class AsyncProcessorConfigurer implements Function<RegionFunctionContext>, Declarable {
	private static final long serialVersionUID = -6538323708175724927L;

	private static final Logger LOG = LoggerFactory.getLogger(AsyncProcessorConfigurer.class);
	private static final String PRIMARY_ON = "PRIMARY-ON";
	private static final String PRIMARY_OFF = "PRIMARY-OFF";
	private static final String ALL = "ALL";

	public static final String PRIMARY_PROP = "primary-async-processor";

	private int distributionId = -1;

	private String memberName;

	private boolean primary = false;

	public AsyncProcessorConfigurer() {
		this.memberName = CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember().getName();
		this.distributionId = AsyncProcessorMonitor.getDistributedSystemId();
		this.primary = Boolean.getBoolean(PRIMARY_PROP);
	}

	public void execute(FunctionContext<RegionFunctionContext> context) {
		if (!(context instanceof RegionFunctionContext))
			throw new FunctionException(
					"The " + this.getClass().getSimpleName() + " function must be called using onRegion");

		RegionFunctionContext rfc = (RegionFunctionContext) context;
		if (rfc.getFilter() == null || rfc.getFilter().size() == 0)
			throw new FunctionException(
					"The " + this.getClass().getSimpleName() + " function must pass keys using the filter");

		List<String> results = new ArrayList<String>();
		String[] args = (String[]) rfc.getArguments();
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (PRIMARY_ON.equals(arg.toUpperCase())) {
					this.primary = true;
				} else if (PRIMARY_OFF.equals(arg.toUpperCase())) {
					this.primary = false;
				}
			}
		}

		Region<String, Integer> localRegion = rfc.getDataSet();
		if (!this.primary && this.distributionId > 0) {
			results.add(processKeys(localRegion, rfc.getFilter(), -1) + " key(s) where made secondary");
		} else if (this.primary && this.distributionId > 0) {
			results.add(processKeys(localRegion, rfc.getFilter(), this.distributionId) + " key(s) where made primary");
		} else {
			if (distributionId == -1) {
				String msg = "Member: " + this.memberName + " - " + AsyncProcessorMonitor.DIST_ID_PROP
						+ " has not been set - unable to process events";
				LOG.warn(msg);
				results.add(msg);
			}
		}
		context.getResultSender().lastResult(results);
	}

	@SuppressWarnings("unchecked")
	private String processKeys(Region<String, Integer> localRegion, Set<?> filter, int id) {
		StringBuilder sb = new StringBuilder();
		Set<String> keys = (Set<String>) filter;
		if (keys != null && keys.size() > 0 && !keys.contains(ALL)) {
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				localRegion.put(key, id);
				sb.append(key + ",");
			}
		} else {
			if (keys != null && keys.size() > 0 && keys.contains(ALL)) {
				Set<AsyncEventQueue> asyncQueues = CacheFactory.getAnyInstance().getAsyncEventQueues();
				Iterator<AsyncEventQueue> it = asyncQueues.iterator();
				while (it.hasNext()) {
					String key = it.next().getAsyncEventListener().getClass().getSimpleName();
					localRegion.put(key, id);
					sb.append(key + ",");
				}
			} else {
				return "Member: " + this.memberName + " - ";
			}
		}
		return "Member: " + this.memberName + " - " + sb.toString().substring(0, sb.toString().length() - 1);
	}

	public String getId() {
		return getClass().getSimpleName();
	}

	public boolean optimizeForWrite() {
		return true;
	}

	public boolean hasResult() {
		return true;
	}
}
