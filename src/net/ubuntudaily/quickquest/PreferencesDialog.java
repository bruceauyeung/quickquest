package net.ubuntudaily.quickquest;

import java.io.File;
import java.util.ArrayList;

import net.ubuntudaily.quickquest.commons.json.JsonHelper;
import net.ubuntudaily.quickquest.preferences.MonitoredDirectory;
import net.ubuntudaily.quickquest.preferences.Preferences;

import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QListView;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStackedWidget;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class PreferencesDialog extends QDialog {

	
	private DirectoryTab directoryTab;
	public PreferencesDialog(Main parent) {
		super(parent);
		this.setWindowTitle(tr(Main.AppName) + tr(" Preferences"));
		QVBoxLayout vboxLayout = new QVBoxLayout(this);
		this.setLayout(vboxLayout);

		QTabWidget tabWidget = new QTabWidget(this);

		// To fit any widget into its parent widget, you need to use a
		// particular layout (grid, horizontal, vertical etc), this conclusion
		// need to be verified.
		// to make QTabWidget auto-resize when QDialog is resized.
		tabWidget.setSizePolicy(QSizePolicy.Policy.Expanding,
				QSizePolicy.Policy.Expanding);
		directoryTab = new DirectoryTab(this);
		tabWidget.addTab(directoryTab, "Directories");

		vboxLayout.addWidget(tabWidget);
		QPushButton okBtn = new QPushButton("OK", this);
		okBtn.clicked.connect(this, "slotOkBtnHandler()");
		okBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		
		QPushButton cancelBtn = new QPushButton("Cancel", this);
		cancelBtn.clicked.connect(this, "accept()");
		cancelBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		
		QPushButton applyBtn = new QPushButton("Apply", this);
		applyBtn.clicked.connect(this, "slotApplyBtnHandler()");
		applyBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		
		QHBoxLayout btnLayout = new QHBoxLayout(this);
		btnLayout.addWidget(okBtn);
		btnLayout.addWidget(cancelBtn);
		btnLayout.addWidget(applyBtn);
		btnLayout.setAlignment(new Qt.Alignment(
				Qt.AlignmentFlag.AlignRight));
		
		vboxLayout.addLayout(btnLayout);
		this.setModal(true);
		
		loadPreferences();

	}

	private void loadPreferences() {
		
		Preferences prefs = QuickQuest.getPreferences();
		directoryTab.getDbLocLineEdit().setText(prefs.getDirectoryTab().getDatabaseLocation().getAbsolutePath());
		
		for(MonitoredDirectory moniDir :prefs.getDirectoryTab().getMonitoredDirectories()){
			directoryTab.addMonitorDir(moniDir.getDirectory().getAbsolutePath(), moniDir.isInSearchPath(), moniDir.isChangesMonitored());
		}
		
	}
	
	private void slotApplyBtnHandler(){
		Preferences prefs = QuickQuest.getPreferences();
		saveDirectoryTab();
		final File jsonFile = new File(QuickQuest.QUICK_QUEST_CFG_DIR, "prefereces.json");
		JsonHelper.toJson(jsonFile, prefs);
	}

	private class DirectoryTab extends QWidget {

		private QLineEdit dbLocLineEdit;
		private QListWidget monitorDirListWidget;
		private QStackedWidget settingsPagesWidget;

		public DirectoryTab(QWidget parent) {
			super(parent);
			QLabel dbLocLable = new QLabel(tr("Database Location:"), this);
			Preferences prefs = QuickQuest.getPreferences();
			dbLocLineEdit = new QLineEdit(prefs.getDirectoryTab().getDatabaseLocation().getAbsolutePath(), this);
			QPushButton browserBtn = new QPushButton(tr("Browse"), this);
			browserBtn.clicked.connect(this, "browse()");

			QLabel monitorDirsLabel = new QLabel(tr("Monitored Directories:"),
					this);

			monitorDirListWidget = new QListWidget(this);
			monitorDirListWidget.setViewMode(QListView.ViewMode.ListMode);
			monitorDirListWidget.setMovement(QListView.Movement.Static);
			monitorDirListWidget.currentRowChanged.connect(this, "slotCurMonDirChanged(int)");
			
			// contentsWidget.setMaximumWidth(128);
			// contentsWidget.setSpacing(12);

			/*
			 * contentsWidget.currentItemChanged.connect(this,
			 * "changePage(QListWidgetItem , QListWidgetItem)");
			 */

			QVBoxLayout dirsBtnLayout = new QVBoxLayout();
			dirsBtnLayout.setContentsMargins(0, 0, 0, 0);
			final QPushButton addMonDirBtn = new QPushButton(tr("&Add"), this);
			addMonDirBtn.clicked.connect(this, "slotAddMonitorDir()");
			dirsBtnLayout.addWidget(addMonDirBtn);
			
			final QPushButton delMonDirBtn = new QPushButton(tr("&Delete"), this);
			delMonDirBtn.clicked.connect(this, "slotDelMonitorDir()");
			dirsBtnLayout.addWidget(delMonDirBtn);
			dirsBtnLayout.setAlignment(new Qt.Alignment(
					Qt.AlignmentFlag.AlignTop));
			QWidget dirBtns = new QWidget(this);
			dirBtns.setLayout(dirsBtnLayout);

			settingsPagesWidget = new QStackedWidget(this);
			
			QGridLayout mainLayout = new QGridLayout();
			mainLayout.addWidget(dbLocLable, 0, 0, 1, 2);
			mainLayout.addWidget(dbLocLineEdit, 1, 0, 1, 1);
			mainLayout.addWidget(browserBtn, 1, 1, 1,1);
			mainLayout.addWidget(monitorDirsLabel, 2, 0, 1, 2);
			mainLayout.addWidget(monitorDirListWidget, 3, 0, 1, 1);
			mainLayout.addWidget(dirBtns, 3, 1, 1, 1);
			mainLayout.addWidget(settingsPagesWidget, 4, 0, 1, 1);
			// mainLayout.addStretch(1);
			setLayout(mainLayout);
		}
	    private QGroupBox createMoniDirSettingsPage(String dirPath, boolean isInSearchPath, boolean changesMonitored)
	    {
	        return new MoniDirSettingsPage(tr("Settings for Directory ") + "("+dirPath+")", isInSearchPath, changesMonitored);
	    }
		private void slotAddMonitorDir() {
			String directory = QFileDialog.getExistingDirectory(this,
					tr("Select a directory to be monitored"), QDir.currentPath());
			if (!directory.equals("")) {
				addMonitorDir(directory, true, true);
			}
		}
		private void addMonitorDir(String directory, boolean isInSearchPath, boolean changesMonitored) {
			QListWidgetItem monitorDirItem = new QListWidgetItem(monitorDirListWidget);
			monitorDirItem.setText(directory);
			monitorDirItem.setTextAlignment(AlignmentFlag.AlignLeft.value());
			monitorDirListWidget.setCurrentItem(monitorDirItem);
			settingsPagesWidget.addWidget(createMoniDirSettingsPage(directory, isInSearchPath, changesMonitored));
			settingsPagesWidget.setCurrentIndex(monitorDirListWidget.currentRow());
		}
		private void slotDelMonitorDir() {
			final QListWidgetItem currentItem = monitorDirListWidget.currentItem();
			monitorDirListWidget.removeItemWidget(currentItem);
			
			// dispose must be called after calling removeItemWidget method, or that item will not be removed. 
			currentItem.dispose();
		}	
		
		private void slotCurMonDirChanged(int row){
			settingsPagesWidget.setCurrentIndex(row);
		}

		private void browse() {
			String directory = QFileDialog.getExistingDirectory(this,
					tr("Find Files"), QDir.currentPath());
			if (!directory.equals("")) {
				dbLocLineEdit.setText(directory);
			}
		}
		public QLineEdit getDbLocLineEdit() {
			return dbLocLineEdit;
		}
		public QListWidget getMonitorDirListWidget() {
			return monitorDirListWidget;
		}
		public QStackedWidget getSettingsPagesWidget() {
			return settingsPagesWidget;
		}
		

	}
	private class MoniDirSettingsPage extends QGroupBox {
		private QCheckBox inSearchPathCtrl;
		private QCheckBox changesMonitoredCtrl;

		public MoniDirSettingsPage(String title, boolean isInSearchPath, boolean changesMonitored) {
			super(title);

	        inSearchPathCtrl = new QCheckBox(tr("Included in search path"));
	        inSearchPathCtrl.setChecked(isInSearchPath);
	        changesMonitoredCtrl = new QCheckBox(tr("Monitor changes"));
	        changesMonitoredCtrl.setChecked(changesMonitored);
	        QVBoxLayout layout = new QVBoxLayout();
	        layout.addWidget(inSearchPathCtrl);
	        layout.addWidget(changesMonitoredCtrl);
	        layout.addStretch(1);
	        this.setLayout(layout);
		}

		public QCheckBox getInSearchPathCtrl() {
			return inSearchPathCtrl;
		}

		public QCheckBox getChangesMonitoredCtrl() {
			return changesMonitoredCtrl;
		}
	}
	private void slotOkBtnHandler(){
		
		slotApplyBtnHandler();
		this.accept();
	}
	private void saveDirectoryTab() {
		Preferences prefs = QuickQuest.getPreferences();
		String databaseLocation = directoryTab.getDbLocLineEdit().text();
		final File newDbLocFile = new File(databaseLocation);
		final net.ubuntudaily.quickquest.preferences.DirectoryTab dirTab = prefs.getDirectoryTab();
		if(dirTab.getOldDatabaseLocation() == null){
			dirTab.setOldDatabaseLocation(dirTab.getDatabaseLocation());
		}
		dirTab.setDatabaseLocation(newDbLocFile);
		
		ArrayList<MonitoredDirectory> moniDirs = new ArrayList<MonitoredDirectory>();
		
		QListWidget moniDirListWidget = directoryTab.getMonitorDirListWidget();
		QStackedWidget settingPages = directoryTab.getSettingsPagesWidget();
		
		for(int i = 0;i < moniDirListWidget.count(); i++){
			QListWidgetItem item = moniDirListWidget.item(i);
			String moniDir = item.text();
			boolean isInSearchPath = true;
			boolean changesMonitored = true;
			QWidget settingsPage = settingPages.widget(i);
			if( settingsPage instanceof MoniDirSettingsPage){
				MoniDirSettingsPage moniDirSettingsPage = (MoniDirSettingsPage) settingsPage;
				isInSearchPath = moniDirSettingsPage.getInSearchPathCtrl().isChecked();
				changesMonitored = moniDirSettingsPage.getChangesMonitoredCtrl().isChecked();
			}
			
			moniDirs.add(new MonitoredDirectory(new File(moniDir), isInSearchPath, changesMonitored));
		}
		dirTab.setMonitoredDirectories(moniDirs);
		//directoryTab
		
	}
}
