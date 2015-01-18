package us.suselinks.quickquest;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.suselinks.quickquest.commons.archive.ZipUtils;
import us.suselinks.quickquest.commons.collections.Lists;
import us.suselinks.quickquest.commons.collections.Maps;
import us.suselinks.quickquest.commons.io.DirectoryWatcher;
import us.suselinks.quickquest.commons.io.FileFindResult;
import us.suselinks.quickquest.commons.io.FileFinder;
import us.suselinks.quickquest.commons.io.FileUtils;
import us.suselinks.quickquest.commons.io.FilenameUtils;
import us.suselinks.quickquest.fsobject.FSObjectTableModel;
import us.suselinks.quickquest.fsobject.FSObjectVO;
import us.suselinks.quickquest.fsobject.FileOperation;
import us.suselinks.quickquest.fsobject.FileOperationFlowController;
import us.suselinks.quickquest.fsobject.FileOperationListener;
import us.suselinks.quickquest.fsobject.ViewModelNotice;
import us.suselinks.quickquest.fsobject.ViewModelNoticeHandler;
import us.suselinks.quickquest.preferences.MonitoredDirectory;
import us.suselinks.quickquest.preferences.Preferences;
import us.suselinks.quickquest.utils.FSObjectTableModelWorker;

import com.trolltech.qt.QtBlockedSlot;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QItemSelectionModel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMessageBox.StandardButton;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;

/*
 * qtablewidget hold cell value.
 *  The most important difference is that model/view widgets do not store
 data behind the table cells. In fact, they operate directly from your data.
 When the view has to know what the cell's text is, it calls the method	
 {QAbstractItemModel::data()}{MyModel::data()}.
 Each time you hover the cursor over the field,
 \l{QAbstractItemModel::}{data()} will be called,That's why it is important to make sure that your data is
 available when \l{QAbstractItemModel::}{data()} is invoked and expensive
 lookup operations are cached.
 */
/**
 * before running this program, you should:
 * <ol>
 * <li>make sure that path of the directory which contains libqtjambi.so is in
 * your <code>java.library.path</code>, for example,
 * <code>/usr/lib/qtjambi/</code>, for linux, you can put this path into
 * <code>LD_LIBRARY_PATH</code></li>
 * <li>make sure that path of the directory which contains jnotify-VER.jar is in
 * your <code>java.library.path</code>, for linux, you can define java variable
 * while starting jvm. java -Djava.library.path=. -jar jnotify-VER.jar</li>
 * </ol>
 * 
 * @author <a href="mailto:bruce.oy@gmail.com">bruce.oy@gmail.com</a>
 * 
 */
public class Main extends QMainWindow {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private QLineEdit questLineEdit;
	private QTableView matchedFilesTableView;
	private QAction aboutQtJambiAct;
	private QAction aboutQtAct;
	private QAction aboutAct;
	private QAction exitAct;

	private QMenu ctxMenu;
	public static final String AppName = "QuickQuest";
	private QAction downloadAct;
	private ExecutorService executor = null;
	private FSObjectTableModel tableModel;
	private Future<?> fsObjectIndexerTask;
	private List<Future<?>> fileFinderTaskList = new ArrayList<Future<?>>();
	private Future<?> dirWatcherTask;
	private DirectoryWatcher dirWatcher;
	private Future<?> noticeHandlerTask;
	private QAction openAct;
	private QAction extractAct;
	private QAction explorePathAct;
	private QAction delelteAct;
	private QAction prefsAct;
	private BlockingQueue<FileOperation> fsoIndexerQueue;
	private BlockingQueue<ViewModelNotice> noticeQueue = new LinkedBlockingQueue<ViewModelNotice>(500);
	static {

		QuickQuest.loadPreferences();
		FileOperationFlowController.start();
		HyperSQLManager.startupDB();
		// HyperSQLManager.dropAllTables();
		HyperSQLManager.createTables(0, 1, 2);

	}

	public Main() {

		this.resize(800, 600);
		this.setWindowTitle(AppName);

		final File quickQuestIcon = new File(QuickQuest.QUICK_QUEST_PROG_DIR, "quickquest-icon-128x128.png");
		if (quickQuestIcon.isFile()) {

			this.setWindowIcon(new QIcon(quickQuestIcon.getAbsolutePath()));// "quickquest-icon-1.png"
		} else {
			this.setWindowIcon(new QIcon("classpath:us/suselinks/quickquest/quickquest-icon-2.png"));// "quickquest-icon-1.png"
		}

		openAct = new QAction(tr("&Open"), this);
		openAct.setShortcut(tr("Ctrl+O"));
		openAct.setStatusTip(tr("open the selected file or directory with default application."));
		openAct.triggered.connect(this, "slotOpenWithDefApp()");

		extractAct = new QAction(tr("&Extract"), this);
		extractAct.setShortcut(tr("Ctrl+X"));
		extractAct.setStatusTip(tr("extract the selected zip file to same directory."));
		extractAct.triggered.connect(this, "slotExtractToSameDir()");

		explorePathAct = new QAction(tr("Explore Path"), this);
		explorePathAct.setShortcut(tr("Ctrl+E"));
		explorePathAct.setStatusTip(tr("open the parent folder with the default browser"));
		explorePathAct.triggered.connect(this, "slotExplorePath()");

		delelteAct = new QAction(tr("Delete"), this);
		explorePathAct.setShortcut(tr("Ctrl+D"));
		delelteAct.setStatusTip(tr("delete selected items"));
		delelteAct.triggered.connect(this, "slotDelete()");

		exitAct = new QAction(tr("E&xit"), this);
		exitAct.setShortcut(tr("Ctrl+Q"));
		exitAct.setStatusTip(tr("Exit the application"));
		exitAct.triggered.connect(this, "close()");

		prefsAct = new QAction(tr("&Preferences"), this);
		prefsAct.setShortcut(tr("Ctrl+P"));
		prefsAct.setStatusTip(tr("edit QuickQuest preferences."));
		prefsAct.triggered.connect(this, "slotEditPreferences()");

		aboutAct = new QAction(tr("&About"), this);
		aboutAct.setStatusTip(tr("Show the application's About box"));
		aboutAct.triggered.connect(this, "about()");

		aboutQtJambiAct = new QAction(tr("About &Qt Jambi"), this);
		aboutQtJambiAct.setStatusTip(tr("Show the Qt Jambi library's About box"));
		aboutQtJambiAct.triggered.connect(QApplication.instance(), "aboutQtJambi()");

		aboutQtAct = new QAction(tr("About Q&t"), this);
		aboutQtAct.setStatusTip(tr("Show the Qt library's About box"));
		aboutQtAct.triggered.connect(QApplication.instance(), "aboutQt()");

		QMenu fileMenu = this.menuBar().addMenu("&File");
		fileMenu.addAction(exitAct);

		QMenu editMenu = this.menuBar().addMenu("&Edit");
		editMenu.addAction(prefsAct);

		QMenu helpMenu = menuBar().addMenu(tr("&Help"));
		helpMenu.addAction(aboutAct);
		helpMenu.addSeparator();
		helpMenu.addAction(aboutQtJambiAct);
		helpMenu.addAction(aboutQtAct);

		questLineEdit = new QLineEdit(this);
		questLineEdit.textChanged.connect(this, "slotQuestTextChanged()");

		// http://doc.qt.digia.com/qtjambi-4.5.2_01/com/trolltech/qt/model-view-model-subclassing.html#read-only-access
		// http://blog.csdn.net/tonylk/article/details/1315053
		// http://www.cppblog.com/yuanyajie/archive/2007/06/15/26387.aspx
		// http://doc.qt.digia.com/qtjambi-4.5.2_01/index.html
		// http://qt-project.org/doc/qt-4.8/itemviews-addressbook.html
		// http://www.qtcentre.org/threads/16794-QTableView-setColumnWidth-not-working
		matchedFilesTableView = new QTableView(this);
		QVBoxLayout vboxLayout = new QVBoxLayout(this);
		vboxLayout.addWidget(questLineEdit);
		vboxLayout.addWidget(matchedFilesTableView);
		QFrame frame = new QFrame(this);
		frame.setLayout(vboxLayout);
		this.setCentralWidget(frame);

		tableModel = new FSObjectTableModel(null);
		tableModel.rowCountChanged.connect(this, "slotRowCountChanged(int)");
		// tableModel.modelReset.connect(this, "slotModelReset()");
		QSortFilterProxyModel proxyModel = new QSortFilterProxyModel(this);
		proxyModel.setSourceModel(tableModel);
		matchedFilesTableView.setModel(proxyModel);

		matchedFilesTableView.setWordWrap(false);
		matchedFilesTableView.verticalHeader().hide();

		// must be called after setModel
		matchedFilesTableView.setColumnWidth(0, 200);
		matchedFilesTableView.setColumnWidth(1, 300);
		matchedFilesTableView.setColumnWidth(2, 100);
		matchedFilesTableView.setColumnWidth(3, 100);
		// tableView.setColumnHidden(4, true);
		// tableView.setColumnHidden(5, true);

		// setStretchLastSection(true) is a performance killer
		// fsObjectTableView.horizontalHeader().setStretchLastSection(true);
		// fsObjectTableView.setSortingEnabled(true);
		// fsObjectTableView.sortByColumn(2, Qt.SortOrder.DescendingOrder);
		matchedFilesTableView.setShowGrid(false);
		matchedFilesTableView.setSelectionBehavior(QTableView.SelectionBehavior.SelectRows);
		LOGGER.debug(matchedFilesTableView.verticalScrollBarPolicy().toString());

		// defaults to Qt.ScrollBarPolicy.ScrollBarAsNeeded
		// fsoTableView.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAsNeeded);

		matchedFilesTableView.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
		matchedFilesTableView.customContextMenuRequested.connect(this, "slotCustomContextMenuRequested(QPoint)");

		matchedFilesTableView.doubleClicked.connect(this, "slotDoubleClicked()");
		ctxMenu = new QMenu(this);
		ctxMenu.addAction(openAct);
		ctxMenu.addAction(extractAct);
		ctxMenu.addAction(explorePathAct);
		ctxMenu.addAction(delelteAct);

		// http://stackoverflow.com/questions/4031168/qtableview-is-extremely-slow-even-for-only-3000-rows
		// resize is a performance killer
		// fsObjectTableView.resizeColumnsToContents();

		int corePoolSize = 8;
		int maximumPoolSize = 16;
		long keepAliveTime = 60;
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(10);
		executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);

		startFindingAndWatching();

		// Path pathToWatch = FileSystems.getDefault().getPath("/mnt/F/");
		// JDK7DirectoryWatcher dirWatcher = new
		// JDK7DirectoryWatcher(pathToWatch);
		// dirWatcherTask = executor.submit(dirWatcher);

		// dirWatcher.startWatch();

		// FSObjectInfo obj = new FSObjectInfo();
		// obj.setName("/");
		// obj.setType(FSObjectType.DIR);
		// obj.setDepth(0);
		// try {
		// HyperSQLManager.insertFSObjectInfo(obj);
		// Map<String, String> cond = new HashMap<>();
		// cond.put("name", "/");
		// List<FSObjectInfo> objs = HyperSQLManager.selectEquals(cond);
		// obj = objs.get(0);
		// FSObjectInfo obj1 = new FSObjectInfo();
		// obj1.setName("home");
		// obj1.setType(FSObjectType.DIR);
		// obj1.setDepth(1);
		// obj1.setPoid(obj.getId());
		//
		// HyperSQLManager.insertFSObjectInfo(obj1);
		//
		// cond = new HashMap<>();
		// cond.put("name", "home");
		// objs = HyperSQLManager.selectEquals(cond);
		// obj = objs.get(0);
		// System.out.println(obj);
		//
		//
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public void startFindingAndWatching() {
		ViewModelNoticeHandler noticeHandler = new ViewModelNoticeHandler(matchedFilesTableView, tableModel, noticeQueue, executor);
		noticeHandlerTask = executor.submit(noticeHandler);

		fsoIndexerQueue = new LinkedBlockingQueue<FileOperation>(1000);
		FileOperationListener fileOperationListener = new FileOperationListener(fsoIndexerQueue);
		List<File> toMonitoredDirsList = Lists.newArrayList();

		List<MonitoredDirectory> moniDirs = QuickQuest.getPreferences().getDirectoryTab().getMonitoredDirectories();
		for (MonitoredDirectory moniDir : moniDirs) {
			if (!moniDir.isFullScanFinished()) {

				final FileFinder fileFinder = new FileFinder(moniDir.getDirectory(), null, -1, 5, fsoIndexerQueue);
				fileFinder.state.connect(this, "slotFileFindFinished(FileFindResult)");
				fileFinderTaskList.add(executor.submit(fileFinder));

			}
			if (moniDir.isChangesMonitored()) {
				toMonitoredDirsList.add(moniDir.getDirectory());
			}
		}

		dirWatcher = new DirectoryWatcher(toMonitoredDirsList, fileOperationListener);
		dirWatcherTask = (Future<?>) executor.submit(dirWatcher);

		// fileFinderThread = new Thread(new FileFinder(new
		// File("/home/bruce/下载"), null, -1, 5, queue));
		// fileFinderThread.start();

		fsObjectIndexerTask = executor.submit(new FSObjectIndexer(fsoIndexerQueue, noticeQueue));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		QApplication.initialize(args);
		final Main main = new Main();
		main.show();
		QApplication.instance().exec();

	}

	public void slotOpenWithDefApp() {
		QItemSelectionModel selModel = matchedFilesTableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();
		List<Integer> rows = getSelectedRows(idxes);
		for (int row : rows) {
			doubleClick(row);
		}
	}

	public void slotModelReset() {
		this.matchedFilesTableView.reset();
	}

	public void slotDoubleClicked() {
		QItemSelectionModel selModel = matchedFilesTableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();
		List<Integer> rows = getSelectedRows(idxes);
		doubleClick(rows.get(0));
	}

	private void doubleClick(int row) {
		FSObjectVO fsovo = tableModel.getRow(row);
		File file = new File(fsovo.getPath(), fsovo.getName());

		if (!FileUtils.openWithDefApp(file)) {

			// TODO: tell user this file can not be opened.
		}
	}

	public void slotExtractToSameDir() {
		QItemSelectionModel selModel = matchedFilesTableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();
		if (idxes.size() > 0) {

			// QWidget widget =
			// tableView.indexWidget(tableModel.index(idxes.get(0).row(), 5));
			// QWidget widget = tableView.indexWidget(idxes.get(0));

			// LOGGER.debug("rowNum:{},poid:{}",.)
			FSObjectVO fsovo = tableModel.getRow(idxes.get(0).row());
			File file = new File(fsovo.getPath(), fsovo.getName());

			Charset charset = ZipUtils.detectZipInternalFileNameCharset(file);
			File destDir = new File(fsovo.getPath(), FilenameUtils.getBaseName(file.getName()));
			ZipUtils.unzip(file, destDir, charset.name());

		}
	}

	public void slotExplorePath() {
		QItemSelectionModel selModel = matchedFilesTableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();

		List<Integer> rows = getSelectedRows(idxes);
		for (int row : rows) {
			FSObjectVO fsovo = tableModel.getRow(row);
			File parent = new File(fsovo.getPath());
			FileUtils.openWithDefApp(parent);
		}
	}

	public void slotDelete() {
		QItemSelectionModel selModel = matchedFilesTableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();
		List<Integer> rows = getSelectedRows(idxes);
		final int size = rows.size();

		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();
		final StandardButton ok = QMessageBox.StandardButton.Ok;
		buttons.set(ok);
		final StandardButton cancel = QMessageBox.StandardButton.Cancel;
		buttons.set(cancel);
		StandardButton clicked = null;

		if (size > 1) {
			clicked = QMessageBox.question(this, tr("Deleting multiple items"), tr("Are you sure to delete these ") + size + tr(" items?"), buttons, ok);
			if (clicked == ok) {
				for (int i = 0; i < size; i++) {
					FSObjectVO fsovo = tableModel.getRow(rows.get(i));
					FileUtils.deleteQuietly(new File(fsovo.getPath(), fsovo.getName()));
				}

			}
		} else if (size == 1) {
			FSObjectVO fsovo = tableModel.getRow(rows.get(0));
			File first = new File(fsovo.getPath(), fsovo.getName());
			if (first.isDirectory()) {

				clicked = QMessageBox.question(this, tr("Deleting directory"), tr("Are you sure to delete this directory?"), buttons, ok);

			} else if (first.isFile()) {
				clicked = QMessageBox.question(this, tr("Deleting file"), tr("Are you sure to delete this file?"), buttons, ok);
			}

			if (clicked == ok) {
				FileUtils.deleteQuietly(first);
			}
		}

	}

	public void slotCustomContextMenuRequested(QPoint p) {

		ctxMenu.exec(QCursor.pos());
	}

	public void slotRowCountChanged(int newRowCount) {
		this.statusBar().showMessage(String.format("%,d Object(s) found.", newRowCount));
	}

	public void slotQuestTextChanged() {
		String critira = questLineEdit.text();
		Map<String, String> conditions = Maps.newHashMapWithExpectedSize(1);
		conditions.put("name", critira);
		executor.submit(new FSObjectTableModelWorker(this, critira));

	}

	public void addMonitoredDirectory(MonitoredDirectory dir) {
		if (dir.isChangesMonitored()) {
			final FileFinder task = new FileFinder(dir.getDirectory(), null, -1, 5, fsoIndexerQueue);
			task.state.connect(this, "slotFileFindFinished(FileFindResult)");
			fileFinderTaskList.add(executor.submit(task));
			dirWatcher.register(dir.getDirectory());
		}
	}

	public void modMonitoredDirectory(MonitoredDirectory dir) {
		// no need to cancel the FileFinder task
		if (!dir.isChangesMonitored()) {
			dirWatcher.unregister(dir.getDirectory());
		}
	}

	public void delMonitoredDirectory(MonitoredDirectory dir) {

		// TODO:cancel the FileFinder task if needed
		dirWatcher.unregister(dir.getDirectory());
	}

	public void slotFileFindFinished(FileFindResult result) {
		if (result.isFullScanFinished()) {
			Preferences prefs = QuickQuest.getPreferences();
			prefs.getDirectoryTab().getMonitoredDirectory(result.getStartDirectory()).setFullScanFinished(true);
			QuickQuest.savePreferencesToDisk();
		}

	}

	public void slotEditPreferences() {
		PreferencesDialog prefsDialog = new PreferencesDialog(this);
		prefsDialog.exec();
	}

	public void about() {
		AboutDialog aboutDialog = new AboutDialog(this);
		aboutDialog.exec();
	}

	@Override
	@QtBlockedSlot
	protected void closeEvent(QCloseEvent qCloseEvent) {
		FileOperationFlowController.stop();
		CachedFileIconProvider.instance().stopPurge();
		for (Future<?> f : fileFinderTaskList) {
			f.cancel(true);
		}
		dirWatcher.unregisterAll();
		fsObjectIndexerTask.cancel(true);
		noticeHandlerTask.cancel(true);
		// TODO:
		// dirWatcherTask.cancel(true);
		// dirWatcher.unregisterAll();
		// dirWatcherTask.cancel(true);
		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LOGGER.debug(e.getMessage());
			}
		}

		HyperSQLManager.shutdownDB();

		super.closeEvent(qCloseEvent);
	}

	public QTableView getTableView() {
		return matchedFilesTableView;
	}

	public FSObjectTableModel getTableModel() {
		return tableModel;
	}

	private List<Integer> getSelectedRows(List<QModelIndex> indexes) {
		List<Integer> rows = Lists.newArrayList();
		for (QModelIndex index : indexes) {

			int row = index.row();
			if (!rows.contains(row)) {
				rows.add(row);
			}
		}
		return rows;
	}
}
