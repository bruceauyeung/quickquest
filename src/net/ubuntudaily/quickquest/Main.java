package net.ubuntudaily.quickquest;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.ubuntudaily.quickquest.commons.archive.ZipUtils;
import net.ubuntudaily.quickquest.commons.collections.Lists;
import net.ubuntudaily.quickquest.commons.io.DirectoryWatcher;
import net.ubuntudaily.quickquest.commons.io.FileFinder;
import net.ubuntudaily.quickquest.commons.io.FileUtils;
import net.ubuntudaily.quickquest.commons.io.FilenameUtils;
import net.ubuntudaily.quickquest.fsobject.FSObjectTableModel;
import net.ubuntudaily.quickquest.fsobject.FSObjectVO;
import net.ubuntudaily.quickquest.fsobject.FileOperation;
import net.ubuntudaily.quickquest.fsobject.FileOperationListener;
import net.ubuntudaily.quickquest.fsobject.ViewModelNotice;
import net.ubuntudaily.quickquest.fsobject.ViewModelNoticeHandler;
import net.ubuntudaily.quickquest.utils.FSObjectTableModelWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <code>/usr/lib/qtjambi/</code>, for linux, you can put this path into <code>LD_LIBRARY_PATH</code></li>
 * <li>make sure that path of the directory which contains  jnotify-VER.jar is in your <code>java.library.path</code>, for linux,
 * you can define java variable while starting jvm. java -Djava.library.path=. -jar jnotify-VER.jar</li>
 * </ol>
 * 
 * @author <a href="mailto:bruce.oy@gmail.com">bruce.oy@gmail.com</a>
 * 
 */
public class Main extends QMainWindow {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private QLineEdit questLineEdit;
	private QTableView tableView;
	private QAction aboutQtJambiAct;
	private QAction aboutQtAct;
	private QAction aboutAct;
	private QAction exitAct;

	private QMenu ctxMenu;
	public static final String AppName = "QuickQuest";
	private QAction downloadAct;
	private ExecutorService executor = null;
	private FSObjectTableModel tableModel;
	private Future<Integer> fileFinderTask;
	private Future<?> fsObjectIndexerTask;
	private Future<?> dirWatcherTask;
	private DirectoryWatcher dirWatcher;
	private Future<?> noticeHandlerTask;
	private QAction openAct;
	private QAction extractAct;
	private QAction prefsAct;
	private static final String QUEST_PATH = "e:\\Movies";
	static {
		HyperSQLManager.startupDB();
		// HyperSQLManager.dropAllTables();
		HyperSQLManager.createTables(0, 1, 2);
	}

	public Main() {

		this.resize(800, 600);
		this.setWindowTitle(AppName);
		this.setWindowIcon(new QIcon(
				"classpath:net/ubuntudaily/quickquest/quickquest-icon-2.png"));// "quickquest-icon-1.png"

		openAct = new QAction(tr("&Open"), this);
		openAct.setShortcut(tr("Ctrl+O"));
		openAct.setStatusTip(tr("open the selected file or directory with default application."));
		openAct.triggered.connect(this, "slotOpenWithDefApp()");
		
		extractAct = new QAction(tr("&Extract"), this);
		extractAct.setShortcut(tr("Ctrl+E"));
		extractAct.setStatusTip(tr("extract the selected zip file to same directory."));
		extractAct.triggered.connect(this, "slotExtractToSameDir()");

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
		aboutQtJambiAct
				.setStatusTip(tr("Show the Qt Jambi library's About box"));
		aboutQtJambiAct.triggered.connect(QApplication.instance(),
				"aboutQtJambi()");

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
		tableView = new QTableView(this);
		QVBoxLayout vboxLayout = new QVBoxLayout(this);
		vboxLayout.addWidget(questLineEdit);
		vboxLayout.addWidget(tableView);
		QFrame frame = new QFrame(this);
		frame.setLayout(vboxLayout);
		this.setCentralWidget(frame);

		tableModel = new FSObjectTableModel(null);
		tableModel.rowCountChanged.connect(this, "slotRowCountChanged(int)");
		QSortFilterProxyModel proxyModel = new QSortFilterProxyModel(this);
		proxyModel.setSourceModel(tableModel);
		tableView.setModel(proxyModel);

		tableView.setWordWrap(false);
		tableView.verticalHeader().hide();

		// must be called after setModel
		tableView.setColumnWidth(0, 200);
		tableView.setColumnWidth(1, 300);
		tableView.setColumnWidth(2, 100);
		tableView.setColumnWidth(3, 100);
		//tableView.setColumnHidden(4, true);
		//tableView.setColumnHidden(5, true);

		// setStretchLastSection(true) is a performance killer
		// fsObjectTableView.horizontalHeader().setStretchLastSection(true);
		// fsObjectTableView.setSortingEnabled(true);
		// fsObjectTableView.sortByColumn(2, Qt.SortOrder.DescendingOrder);
		tableView.setShowGrid(false);
		tableView.setSelectionBehavior(QTableView.SelectionBehavior.SelectRows);
		LOGGER.debug(tableView.verticalScrollBarPolicy().toString());

		// defaults to Qt.ScrollBarPolicy.ScrollBarAsNeeded
		// fsoTableView.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAsNeeded);

		tableView.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
		tableView.customContextMenuRequested.connect(this,
				"slotCustomContextMenuRequested(QPoint)");

		ctxMenu = new QMenu(this);
		ctxMenu.addAction(openAct);
		ctxMenu.addAction(extractAct);
		

		// http://stackoverflow.com/questions/4031168/qtableview-is-extremely-slow-even-for-only-3000-rows
		// resize is a performance killer
		// fsObjectTableView.resizeColumnsToContents();

		int corePoolSize = 8;
		int maximumPoolSize = 8;
		long keepAliveTime = 60;
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(10);
		executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, TimeUnit.SECONDS, workQueue);

		BlockingQueue<ViewModelNotice> noticeQueue = new LinkedBlockingQueue<>(
				500);

		ViewModelNoticeHandler noticeHandler = new ViewModelNoticeHandler(
				tableView, tableModel, noticeQueue);
		noticeHandlerTask = executor.submit(noticeHandler);

		BlockingQueue<FileOperation> queue = new LinkedBlockingQueue<FileOperation>(
				1000);

		fileFinderTask = executor.submit(new FileFinder(new File(QUEST_PATH),
				null, -1, 5, queue));
		// fileFinderThread = new Thread(new FileFinder(new
		// File("/home/bruce/下载"), null, -1, 5, queue));
		// fileFinderThread.start();

		fsObjectIndexerTask = executor.submit(new FSObjectIndexer(queue,
				noticeQueue));

		// Path pathToWatch = FileSystems.getDefault().getPath("/mnt/F/");
		// JDK7DirectoryWatcher dirWatcher = new
		// JDK7DirectoryWatcher(pathToWatch);
		// dirWatcherTask = executor.submit(dirWatcher);

		FileOperationListener fileOperationListener = new FileOperationListener(
				queue);
		List<File> dirList = Lists.newArrayList();
		dirList.add(new File(QUEST_PATH));
		dirWatcher = new DirectoryWatcher(dirList, fileOperationListener);
		dirWatcherTask = (Future<?>) executor.submit(dirWatcher);
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
		QItemSelectionModel selModel = tableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();
		if (idxes.size() > 0) {

			//QWidget widget = tableView.indexWidget(tableModel.index(idxes.get(0).row(), 5));
			//QWidget widget = tableView.indexWidget(idxes.get(0));
			
			//LOGGER.debug("rowNum:{},poid:{}",.)
			FSObjectVO fsovo = tableModel.getRow(idxes.get(0).row());
			File file = new File(fsovo.getPath(), fsovo.getName());

			if (!FileUtils.openWithDefApp(file)) {

				// TODO: tell user this file can not be opened.
			}

		}
	}
	public void slotExtractToSameDir() {
		QItemSelectionModel selModel = tableView.selectionModel();
		List<QModelIndex> idxes = selModel.selection().indexes();
		if (idxes.size() > 0) {

			//QWidget widget = tableView.indexWidget(tableModel.index(idxes.get(0).row(), 5));
			//QWidget widget = tableView.indexWidget(idxes.get(0));
			
			//LOGGER.debug("rowNum:{},poid:{}",.)
			FSObjectVO fsovo = tableModel.getRow(idxes.get(0).row());
			File file = new File(fsovo.getPath(), fsovo.getName());

			Charset charset = ZipUtils.detectZipInternalFileNameCharset(file);
			File destDir = new File(fsovo.getPath(),FilenameUtils.getBaseName(file.getName()));
			ZipUtils.unzip(file, destDir, charset.name());

		}
	}
	
	public void slotCustomContextMenuRequested(QPoint p) {

		ctxMenu.exec(QCursor.pos());
	}

	public void slotRowCountChanged(int newRowCount) {
		this.statusBar().showMessage(
				String.format("%,d Object(s) found.", newRowCount));
	}

	public void slotQuestTextChanged() {
		String critira = questLineEdit.text();
		Map<String, String> conditions = new HashMap<>();
		conditions.put("name", critira);
		executor.submit(new FSObjectTableModelWorker(this, critira));

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
		fileFinderTask.cancel(true);
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
		return tableView;
	}

	public FSObjectTableModel getTableModel() {
		return tableModel;
	}
}
