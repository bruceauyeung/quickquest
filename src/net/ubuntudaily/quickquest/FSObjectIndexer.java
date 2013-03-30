package net.ubuntudaily.quickquest;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import net.ubuntudaily.quickquest.commons.io.FileUtils;
import net.ubuntudaily.quickquest.commons.io.FilenameUtils;
import net.ubuntudaily.quickquest.fsobject.FSObject;
import net.ubuntudaily.quickquest.fsobject.FileOperation;
import net.ubuntudaily.quickquest.fsobject.FileOperationType;
import net.ubuntudaily.quickquest.fsobject.ViewModelNotice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSObjectIndexer implements Callable<Void> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FSObjectIndexer.class);
	private final BlockingQueue<FileOperation> readInQueue;
	private final BlockingQueue<ViewModelNotice> noticeQueue;
	public FSObjectIndexer(BlockingQueue<FileOperation> readInQueue,BlockingQueue<ViewModelNotice> noticeQueue) {
		this.readInQueue = readInQueue;
		this.noticeQueue = noticeQueue;
	}

	private void index(FileOperation fo) {
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
			File before = fo.getBeforeOperated();
			File after = fo.getAfterOperated();
			updateFile(before, after);
		}
		
		
		

	}

	private void notifyViewModel(FileOperationType type, FSObject fSObject) {
		try {
			LOGGER.debug("start emitting a notice .");
			this.noticeQueue.put(new ViewModelNotice(type, null, fSObject));
			LOGGER.debug("finish emitting a notice.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateFile(File before, File after) {
		// TODO Auto-generated method stub

	}

	private void renameFile(File before, File after) {
		// TODO Auto-generated method stub

	}

	private void deleteFile(File file) {
		HyperSQLManager
				.ensureTableExistence(FileUtils.calculateFileDepth(file));
		FSObject fsObjInfo = HyperSQLManager.findEquivalent(file);
		if (fsObjInfo != null) {
			HyperSQLManager.delete(fsObjInfo);
			LOGGER.debug("FSObject removed from database : {}", fsObjInfo.toString());
			
			notifyViewModel(FileOperationType.DELETE, fsObjInfo);
			
		} else {
			LOGGER.warn(
					"file existing in database is expected, but not found, maybe indexes is not consistent with file system.\r\nfile path:{} ",
					file.getAbsolutePath());
		}
	}

	private void insertFile(File file) {

		HyperSQLManager
				.ensureTableExistence(FileUtils.calculateFileDepth(file));

		FSObject fsObjInfo = HyperSQLManager.findEquivalent(file);
		if (fsObjInfo == null) {
			List<String> splittedFilenames = FilenameUtils.split(file
					.getAbsolutePath());

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
				}
			}

		}
	}

	@Override
	public Void call() throws Exception {
		while (!Thread.currentThread().isInterrupted()) {
			FileOperation poll = null;
			try {
				LOGGER.debug("start taking a file operation.");
				poll = this.readInQueue.take();
				LOGGER.debug("finish taking a file operation.");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt();
			}
			index(poll);

		}
		return null;
	}
}
