package net.ubuntudaily.quickquest;

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
		DirectoriesPage dirPage = new DirectoriesPage(this);
		tabWidget.addTab(dirPage, "Directories");

		vboxLayout.addWidget(tabWidget);
		// vboxLayout.setAlignment(tabWidget, Qt.AlignmentFlag.AlignCenter);
		QPushButton okBtn = new QPushButton("OK", this);
		okBtn.clicked.connect(this, "accept()");
		okBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		
		QPushButton cancelBtn = new QPushButton("Cancel", this);
		cancelBtn.clicked.connect(this, "accept()");
		cancelBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		
		QPushButton applyBtn = new QPushButton("Apply", this);
		applyBtn.clicked.connect(this, "accept()");
		applyBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		
		QHBoxLayout btnLayout = new QHBoxLayout(this);
		btnLayout.addWidget(okBtn);
		btnLayout.addWidget(cancelBtn);
		btnLayout.addWidget(applyBtn);
		btnLayout.setAlignment(new Qt.Alignment(
				Qt.AlignmentFlag.AlignRight));
		
		vboxLayout.addLayout(btnLayout);
		this.setModal(true);

	}

	private class DirectoriesPage extends QWidget {

		private QLineEdit dbLocLineEdit;
		private QListWidget monitorDirListWidget;
		private QStackedWidget settingsPagesWidget;

		public DirectoriesPage(QWidget parent) {
			super(parent);
			QLabel dbLocLable = new QLabel(tr("Database Location:"), this);
			dbLocLineEdit = new QLineEdit(this);
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
	    private QGroupBox createTopLeftGroupBox(String dirPath)
	    {
	        QGroupBox topLeftGroupBox = new QGroupBox(tr("Settings for Directory ") + "("+dirPath+")");

	        QCheckBox incInSearchPath = new QCheckBox(tr("Included in search path"));
	        incInSearchPath.setChecked(true);
	        QCheckBox monitorChange = new QCheckBox(tr("Monitor changes"));
	        monitorChange.setChecked(true);
	        QVBoxLayout layout = new QVBoxLayout();
	        layout.addWidget(incInSearchPath);
	        layout.addWidget(monitorChange);
	        layout.addStretch(1);
	        topLeftGroupBox.setLayout(layout);
	        return topLeftGroupBox;
	    }
		private void slotAddMonitorDir() {
			String directory = QFileDialog.getExistingDirectory(this,
					tr("Select a directory to be monitored"), QDir.currentPath());
			if (!directory.equals("")) {
				QListWidgetItem monitorDirItem = new QListWidgetItem(monitorDirListWidget);
				monitorDirItem.setText(directory);
				monitorDirItem.setTextAlignment(AlignmentFlag.AlignLeft.value());
				monitorDirListWidget.setCurrentItem(monitorDirItem);
				settingsPagesWidget.addWidget(createTopLeftGroupBox(directory));
				settingsPagesWidget.setCurrentIndex(monitorDirListWidget.currentRow());
			}
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
	}
}
