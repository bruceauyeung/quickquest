package net.ubuntudaily.quickquest.commons.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import net.ubuntudaily.quickquest.fsobject.FileOperation;
import net.ubuntudaily.quickquest.fsobject.FileOperationType;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.gui.QApplication;

public class FileFinder extends DirectoryWalker<FileOperation> implements
		Callable<FileFindResult> {

	private static final Logger logger = LoggerFactory
			.getLogger(FileFinder.class);
	private FileFilter filter;
	private BlockingQueue<FileOperation> queue;

	private volatile boolean cancelled = false;
	private File startDirectory;
	private int initDepth;
	private int delaySeconds;
	private int founds = 0;
	public final FullScanState state = new FullScanState();

	public void cancel() {
		cancelled = true;
	}

	protected boolean handleIsCancelled(File file, int depth,
			Collection<FileOperation> results) throws IOException {
		if (Thread.currentThread().isInterrupted()) {
			cancelled = true;
		}
		return cancelled;
	}

	public FileFinder(File startDirectory, FileFilter filter, int depthLimit,
			int delaySeconds, BlockingQueue<FileOperation> queue) {
		super(null, depthLimit);
		this.filter = filter == null ? FileFilterUtils.trueFileFilter()
				: filter;
		this.queue = queue;
		this.startDirectory = startDirectory;
		this.initDepth = FileUtils.calculateFileDepth(startDirectory);
		this.delaySeconds = delaySeconds;
	}

	public FileFinder(File startDirectory, FileFilter filter, int depthLimit,
			BlockingQueue<FileOperation> queue) {
		this(startDirectory, filter, depthLimit, 0, queue);
	}

	public void find() {
		try {
			walk(this.startDirectory, this.queue);
			state.setState(FullScanState.FINISHED);
		} catch (IOException ex) {
			if (ex instanceof CancelException) {
				logger.error(
						"walking through the specified directory is cancelled.",
						ex.getCause());
			} else {

				logger.error("failed to walk through the specified directory.",
						ex.getCause());
			}
		}

	}

	protected void handleDirectoryStart(File directory, int depth,
			Collection<FileOperation> results) {

		if (this.filter.accept(directory)) {
			try {
				FileOperation fo = new FileOperation(FileOperationType.CREATE,
						null, directory);
				this.queue.put(fo);
				founds++;
				Thread.sleep(20);
			} catch (InterruptedException e) {
				cancelled = true;
			}
		}
	}

	protected void handleFile(File file, int depth,
			Collection<FileOperation> results) {
		if (this.filter.accept(file)) {
			try {
				FileOperation fo = new FileOperation(FileOperationType.CREATE,
						null, file);
				this.queue.put(fo);
				founds++;
			} catch (InterruptedException e) {
				cancelled = true;
			}
		}
	}

	@Override
	public FileFindResult call() throws Exception {
		if (this.delaySeconds > 0) {
			try {
				Thread.sleep(this.delaySeconds * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		find();
		return new FileFindResult(startDirectory,
				state.getState() == FullScanState.FINISHED, founds);
	}

	public class FullScanState extends QSignalEmitter {
		public static final int FINISHED = 0;
		public static final int RUNNING = 1;
		public static final int UNSTARTED = 2;

		private int state = UNSTARTED;

		public final Signal1<FileFindResult> fullScanFinished = new Signal1<FileFindResult>();

		private int getState() {
			return state;
		}

		private void setState(int state) {
			this.state = state;
			if (this.state == FINISHED) {
				
				// even that event loop is not started, this signal will not be discarded.
				QApplication.invokeLater(new Runnable() {

					@Override
					public void run() {
						fullScanFinished.emit(new FileFindResult(
								startDirectory, true, founds));
					}
				});
			}
		}

		public void connect(Object receiver, String method) {
			fullScanFinished.connect(receiver, method);
		}
	}
}
