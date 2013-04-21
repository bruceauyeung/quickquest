package net.ubuntudaily.quickquest.commons.io;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher implements Callable<Void>{
	private static final Logger LOG=LoggerFactory.getLogger(DirectoryWatcher.class);
	private ConcurrentHashMap<File, Integer> watchedDirs = new ConcurrentHashMap<>();
	private final List<File> toWatchDirs;
	private JNotifyListener listener;


	public DirectoryWatcher(List<File> dirList, JNotifyListener listener){
		toWatchDirs = dirList;
		this.listener = listener;
	}

	public void startWatch(){
		registerAll();
	}
	public void stopWatch(){
		unregisterAll();
	}
	public boolean unregister(File dir){
		boolean succ = false;
		if (watchedDirs.containsKey(dir)) {
			
			try {
				succ = JNotify.removeWatch(watchedDirs.get(dir));
				if(succ){
					watchedDirs.remove(dir);
				}
			} catch (JNotifyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		return succ;
	}
	public boolean register(File dir, JNotifyListener listener) {
		if(dir == null || !dir.isDirectory() || listener == null){
			return false;
		}
		if (!watchedDirs.containsKey(dir)) {
			int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
					| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
			boolean watchSubtree = true;
			int watchID = 0;
			try {
				watchID = JNotify.addWatch(dir.getAbsolutePath(), mask,
						watchSubtree, listener);
				watchedDirs.put(dir, Integer.valueOf(watchID));
			} catch (Throwable t) {
				
				LOG.error(t.getMessage());
				watchedDirs.remove(dir);
			}

			if (watchID > 0) {
				return true;
			}
			return false;
		}
		return false;
	}

	public void registerAll(){
		 
		 for(File f :toWatchDirs){
			 if(Thread.currentThread().isInterrupted()){
				 break;
			 }
			 register(f, this.listener);
		 }
	}
	public void unregisterAll() {
		 Set<Entry<File, Integer>> entrySet = watchedDirs.entrySet();
		 for(Entry<File, Integer>entry :entrySet){
			 unregister(entry.getKey());
		 }
		
	}


	@Override
	public Void call() throws Exception {
		long start = System.nanoTime();
		startWatch();
		LOG.debug("total time for starting watch(ms):{}", (System.nanoTime() - start)/1000000);
		return null;
	}

}
