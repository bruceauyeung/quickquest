package us.suselinks.quickquest;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.suselinks.quickquest.commons.io.FileUtils;
import us.suselinks.quickquest.commons.io.FilenameUtils;
import us.suselinks.quickquest.fsobject.FSObject;
import us.suselinks.quickquest.fsobject.FileOperation;
import us.suselinks.quickquest.fsobject.FileOperationType;
import us.suselinks.quickquest.fsobject.ViewModelNotice;

public class FSObjectIndexer implements Callable<Void> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FSObjectIndexer.class);
	private final BlockingQueue<FileOperation> readInQueue;
	private final BlockingQueue<ViewModelNotice> noticeQueue;

	public FSObjectIndexer(BlockingQueue<FileOperation> readInQueue, BlockingQueue<ViewModelNotice> noticeQueue) {
		this.readInQueue = readInQueue;
		this.noticeQueue = noticeQueue;
	}

	private void index(FileOperation fo) throws InterruptedException {
		if (fo == null) {

			return;
		}
		if (FileOperationType.CREATE.equals(fo.getType())) {
			File file = fo.getAfterOperated();
			insertFile(file);
		} else if (FileOperationType.DELETE.equals(fo.getType())) {
			File file = fo.getBeforeOperated();
			deleteFile(file);

		} else if (FileOperationType.RENAME.equals(fo.getType())) {
			File before = fo.getBeforeOperated();
			File after = fo.getAfterOperated();
			renameFile(before, after);
		} else if (FileOperationType.MODIFY.equals(fo.getType())) {
			File after = fo.getAfterOperated();
			insertOrUpdateFile(after);
		}

	}

	private void notifyViewModel(FileOperationType type, FSObject fSObject) throws InterruptedException {
		LOGGER.debug("start emitting a notice .");
		this.noticeQueue.put(new ViewModelNotice(type, null, fSObject));
		LOGGER.debug("finish emitting a notice.");
	}

	private void insertOrUpdateFile(File after) throws InterruptedException {
		HyperSQLManager.ensureTableExistence(FileUtils.calculateFileDepth(after));
		FSObject fsObjInfo = HyperSQLManager.findEquivalent(after);
		if (fsObjInfo != null) {

			// only size and last modified timestamp are needed to be updated
			fsObjInfo.setSize(after.length());
			fsObjInfo.setLmts(new Timestamp(after.lastModified()));
			try {
				HyperSQLManager.update(fsObjInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.debug("FSObject in database has been updated: {}", fsObjInfo.toString());

			notifyViewModel(FileOperationType.MODIFY, fsObjInfo);

		} else {
			LOGGER.warn("this file's FSObject does not exist yet, try to create one for :{}", after.getAbsolutePath());
			this.insertFile(after);
		}

	}

	private void renameFile(File before, File after) throws InterruptedException {
		this.deleteFile(before);
		this.insertOrUpdateFile(after);
	}

	private void deleteFile(File file) throws InterruptedException {
		HyperSQLManager.ensureTableExistence(FileUtils.calculateFileDepth(file));
		FSObject fsObjInfo = HyperSQLManager.findEquivalent(file);
		if (fsObjInfo != null) {
			HyperSQLManager.delete(fsObjInfo);
			LOGGER.debug("FSObject removed from database : {}", fsObjInfo.toString());

			notifyViewModel(FileOperationType.DELETE, fsObjInfo);

		} else {
			LOGGER.warn("file existing in database is expected, but not found, maybe indexes is not consistent with file system.\r\nfile path:{} ",
					file.getAbsolutePath());

		}
	}

	private void insertFile(File file) throws InterruptedException {

		HyperSQLManager.ensureTableExistence(FileUtils.calculateFileDepth(file));

		FSObject fsObjInfo = HyperSQLManager.findEquivalent(file);
		if (fsObjInfo == null) {
			List<String> splittedFilenames = FilenameUtils.split(file.getAbsolutePath());

			File self = null;
			FSObject selfObj = null;
			File parent = null;
			FSObject parentObj = null;
			for (int i = 0; i < splittedFilenames.size(); i++) {

				parentObj = selfObj;
				parent = self;
				String filename = splittedFilenames.get(i);
				if (parent == null) {

					self = new File(filename);
				} else {
					self = new File(parent, filename);
				}

				selfObj = HyperSQLManager.findEquivalent(self);

				if (selfObj == null) {

					try {
						selfObj = HyperSQLManager.insert(parentObj, self);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Thread.currentThread().interrupt();
					}

					if (selfObj != null) {

						notifyViewModel(FileOperationType.CREATE, selfObj);
					}
				}
			}

		}
	}

	@Override
	public Void call() throws Exception {
		while (!Thread.currentThread().isInterrupted()) {

			try {
				FileOperation poll = null;
				LOGGER.debug("start taking a file operation.");
				poll = this.readInQueue.take();
				LOGGER.debug("finish taking a file operation.");
				index(poll);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt();
			}

		}
		return null;
	}
}
