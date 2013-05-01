package net.ubuntudaily.quickquest;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.ubuntudaily.quickquest.commons.collections.Lists;

import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.gui.QFileIconProvider;
import com.trolltech.qt.gui.QIcon;

public class CachedFileIconProvider {

	private ConcurrentHashMap<String, CachedFileIcon> cachedFileIcons = new ConcurrentHashMap<String, CachedFileIcon>();
	private Lock exclusiveWriteLock = new ReentrantLock();
	private CachedFileIconPurgeTask purgeTask = new CachedFileIconPurgeTask();
	private QFileIconProvider delegate = new QFileIconProvider();
	private final Thread purgeThread;
	private static final CachedFileIconProvider cfip = new CachedFileIconProvider();

	private CachedFileIconProvider() {
		purgeThread = new Thread(purgeTask, "CachedFileIconPurgeTask");
		purgeThread.start();
	}

	public static CachedFileIconProvider instance() {
		return cfip;
	}

	public void stopPurge() {
		purgeThread.interrupt();
	}

	public final QIcon icon(File file) {

		final String filePath = file.getAbsolutePath();
		CachedFileIcon icon = cachedFileIcons.get(filePath);
		if (icon == null) {
			exclusiveWriteLock.lock();

			try {
				icon = cachedFileIcons.get(filePath);
				if (icon == null) {

					QFileInfo fileInfo = new QFileInfo(filePath);
					QIcon toCache = delegate.icon(fileInfo);
					CachedFileIcon cfi = new CachedFileIcon(toCache,
							System.nanoTime());
					cachedFileIcons.put(filePath, cfi);
					icon = cfi;
				}
			} finally {
				exclusiveWriteLock.unlock();
			}

		}
		return icon.getIcon();
	}

	private class CachedFileIconPurgeTask implements Runnable {

		// the unit is millisecond
		private static final int PURGE_PERIOD = 5000;
		private static final int CACHE_SIZE_THRESHOLD = 100;
		private volatile boolean cancelled = false;

		@Override
		public void run() {

			while (!cancelled) {
				final int size = cachedFileIcons.size();
				if (size > CACHE_SIZE_THRESHOLD) {
					int toPurges = size - CACHE_SIZE_THRESHOLD;
					Collection<CachedFileIcon> values = cachedFileIcons
							.values();
					List<CachedFileIcon> copy = Lists
							.newArrayListWithCapacity(values.size());
					copy.addAll(values);
					Collections.sort(copy);
					for (int i = 0; i < toPurges; i++) {
						values.remove(copy.get(i));
					}

				}

				try {
					Thread.sleep(PURGE_PERIOD);
				} catch (InterruptedException e) {
					cancelled = true;
				}
			}
		}

	}

	private class CachedFileIcon implements Comparable<CachedFileIcon> {
		private QIcon icon;
		private long lastAccessTime;

		public CachedFileIcon(QIcon icon, long lastAccessTime) {
			super();
			this.icon = icon;
			this.lastAccessTime = lastAccessTime;
		}

		public QIcon getIcon() {
			return icon;
		}

		public long getLastAccessTime() {
			return lastAccessTime;
		}

		public void setLastAccessTime(long lastAccessTime) {
			this.lastAccessTime = lastAccessTime;
		}

		@Override
		public int compareTo(CachedFileIcon other) {
			if (this.lastAccessTime < other.lastAccessTime) {
				return -1;
			} else if (this.lastAccessTime == other.lastAccessTime) {
				return 0;
			} else {
				return 1;
			}
		}
	}

}
