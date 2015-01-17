package us.suselinks.quickquest.fsobject;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperationFlowController {

	private static final int MODIFY_THRESHOLD_PER_SEC = 1;
	private static final int CREATE_THRESHOLD_PER_SEC = 5;
	private static final long FLOW_RESET_PERIOD = 1000000000;
	private static final ConcurrentHashMap<FSObject, ModifyCountInfo> modifyCountMap = new ConcurrentHashMap<FSObject, ModifyCountInfo>();
	private static final AtomicInteger createCount = new AtomicInteger(0);
	private static final FileOperationFlowResetTask flowResetTask = new FileOperationFlowResetTask();
	private static final ModifyCountMapPurgeTask purgeTask = new ModifyCountMapPurgeTask();
	private static Thread resetThread;
	private static Thread purgeThread;
	
	public static boolean modifyAllowed(FSObject fso){
		final ModifyCountInfo init = new ModifyCountInfo();
		ModifyCountInfo pre = modifyCountMap.putIfAbsent(fso, init);
		
		AtomicInteger cur = pre == null ? init.getModifyCount() : pre.getModifyCount();
		
		int current = cur.incrementAndGet();
		if(MODIFY_THRESHOLD_PER_SEC < current){
			return false;
		}else{
			return true;
		}
	}
	public static boolean createAllowed(){
		int current = createCount.incrementAndGet();
		if(CREATE_THRESHOLD_PER_SEC < current){
			return false;
		}else{
			return true;
		}
		
	}
	private FileOperationFlowController() {
	}
	
	public static final void start(){
		resetThread = new Thread(flowResetTask, "FileOperationFlowResetTask");
		resetThread.start();
		purgeThread = new Thread(purgeTask, "ModifyCountMapPurgeTask");
		purgeThread.start();
	}
	public static final void stop(){
		
		resetThread.interrupt();
		purgeThread.interrupt();
	}
	private static class ModifyCountInfo{
		private AtomicInteger modifyCount = new AtomicInteger(0);
		private AtomicInteger sequentialZeroTimes = new AtomicInteger(0);
		public AtomicInteger getModifyCount() {
			return modifyCount;
		}
		public AtomicInteger getSequentialZeroTimes() {
			return sequentialZeroTimes;
		}
	}
	private static class ModifyCountMapPurgeTask implements Runnable{

		private static final Logger LOG = LoggerFactory.getLogger(ModifyCountMapPurgeTask.class);
		private static final long PURGE_PERIOD = 1000 * 60;
		private static final int MAX_SEQUENTIAL_ZERO_TIMES = 10;
		private static final ConcurrentHashMap<String, AtomicInteger> sequentialZeroTimeMap = new ConcurrentHashMap<String, AtomicInteger>();
		private volatile boolean cancelled = false;
		@Override
		public void run() {
			while (!cancelled ) {
				Set<Entry<FSObject, ModifyCountInfo>> entries = modifyCountMap
						.entrySet();
				for (Entry<FSObject, ModifyCountInfo> entry : entries) {

					ModifyCountInfo info = entry.getValue();
					if (info.getModifyCount().get() == 0) {
						info.getSequentialZeroTimes().incrementAndGet();
					} else {
						info.getSequentialZeroTimes().set(0);
					}

					if (info.getSequentialZeroTimes().get() > MAX_SEQUENTIAL_ZERO_TIMES) {
						entries.remove(entry);
					}
				}
				try {
					Thread.sleep(PURGE_PERIOD);
				} catch (InterruptedException e) {
					cancelled = true;
					LOG.debug(e.getMessage());
				}
			}
		}
		
	}
	private static class FileOperationFlowResetTask implements Runnable{
		private static final Logger LOG = LoggerFactory.getLogger(FileOperationFlowResetTask.class);
		private AtomicLong lastResetTime = new AtomicLong(System.nanoTime());
		
		
		
		private volatile boolean cancelled = false;

		@Override
		public void run() {
			while (!cancelled) {
				long current = System.nanoTime();
				if ((current - lastResetTime.get()) > FLOW_RESET_PERIOD) {
					Collection<ModifyCountInfo> values = modifyCountMap.values();
					for(ModifyCountInfo value : values){
						value.getModifyCount().set(0);
					}
					createCount.set(0);
					lastResetTime.set(current);
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						cancelled = true;
						LOG.debug(e.getMessage());
						
					}
				}
			}
			
		}
		
	}
}
