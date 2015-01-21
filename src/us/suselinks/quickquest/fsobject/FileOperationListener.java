package us.suselinks.quickquest.fsobject;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import net.contentobjects.jnotify.JNotifyListener;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.suselinks.quickquest.FSObjectIndexer;

public class FileOperationListener implements JNotifyListener {

	private static final Logger LOG = LoggerFactory.getLogger(FileOperationListener.class);
	private BlockingQueue<FileOperation> queue;

	public FileOperationListener(BlockingQueue<FileOperation> queue) {
		super();
		Validate.notNull(queue);
		this.queue = queue;
	}

	@Override
	public void fileCreated(int wd, String rootPath, String name) {
		File created = getFile(rootPath, name);
		FileOperation fo = new FileOperation(FileOperationType.CREATE, null, created);
		writeIntoQueue(fo);

	}

	private void writeIntoQueue(FileOperation fo) {
		try {
			LOG.debug("start to insert file operation into queue :{}", fo.toString());
			this.queue.put(fo);
			LOG.debug("finish to insert file operation into queue :{}", fo.toString());
		} catch (InterruptedException e) {
			// TODO this newly created file can only be reindexed now, maybe i
			// should consider record it into a file,
			// then i can handle it when lauching the application.
			e.printStackTrace();

		}
	}

	private File getFile(String rootPath, String name) {
		File parent = new File(rootPath);
		if (name.endsWith(File.separator)) {
			name = name.substring(0, name.lastIndexOf(File.separator));
		}
		File created = new File(parent, name);
		return created;
	}

	@Override
	public void fileDeleted(int wd, String rootPath, String name) {
		File deleted = getFile(rootPath, name);
		FileOperation fo = new FileOperation(FileOperationType.DELETE, deleted, null);
		writeIntoQueue(fo);
	}

	/**
	 * when an index entry exists in QuickQuest database meanwhile the
	 * corresponding real file system entry doesn't, send a
	 * {@link MockFileOperation} object to {@link FSObjectIndexer},then database
	 * index entry will be deleted and the view will get noticed.
	 * 
	 * @param deleted
	 */
	public void mockFileDeleted(File deleted) {
		FileOperation fo = new MockFileOperation(FileOperationType.DELETE, deleted, null);
		writeIntoQueue(fo);
	}

	@Override
	public void fileModified(int wd, String rootPath, String name) {
		File modified = getFile(rootPath, name);
		FileOperation fo = new FileOperation(FileOperationType.MODIFY, null, modified);
		writeIntoQueue(fo);
	}

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
		File oldFile = getFile(rootPath, oldName);
		File newFile = getFile(rootPath, newName);
		FileOperation fo = new FileOperation(FileOperationType.RENAME, oldFile, newFile);

		writeIntoQueue(fo);
	}

}
