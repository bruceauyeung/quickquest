package us.suselinks.quickquest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.commons.io.FilenameUtils;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.ScrollBarPolicy;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QVBoxLayout;

public class AboutDialog extends QDialog {

	public AboutDialog(Main parent) {
		super(parent);
		this.setWindowTitle(tr("About ") + tr(parent.AppName));
		QVBoxLayout vboxLayout = new QVBoxLayout(this);
		this.setLayout(vboxLayout);

		QTabWidget tabWidget = new QTabWidget(this);

		// To fit any widget into its parent widget, you need to use a
		// particular layout (grid, horizontal, vertical etc), this conclusion
		// need to be verified.
		// to make QTabWidget auto-resize when QDialog is resized.
		tabWidget.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding);
		QScrollArea scrollArea = new QScrollArea(this);
		tabWidget.addTab(scrollArea, "General");
		scrollArea.setHorizontalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOff);
		scrollArea.setVerticalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOn);
		// scrollArea.setBackgroundRole(com.trolltech.qt.gui.QPalette.ColorRole.Dark);
		QLabel label = new QLabel(this);
		String version = retrieveQuickQuestVersion();

		label.setText(tr("<b>" + tr(parent.AppName) + "</b> &copy;Bruce Auyeung<br/>"
				+ "(<a href=\"mailto:bruce.auyeung@yahoo.com\">bruce.auyeung@yahoo.com</a>)" + "<p><b>Version : " + version + "</b>"
				+ "<p>Visit my website to get update :<br/>" + "<a href=\"http://www.suselinks.us\">http://www.suselinks.us</a>"
				+ "<p>This program intends to help you to  find files or directories as fast as lightening"));
		label.setWordWrap(true);
		label.setMargin(5);
		label.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding);
		scrollArea.setWidget(label);

		vboxLayout.addWidget(tabWidget);
		// vboxLayout.setAlignment(tabWidget, Qt.AlignmentFlag.AlignCenter);
		QPushButton okBtn = new QPushButton("OK", this);
		okBtn.clicked.connect(this, "accept()");
		okBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		vboxLayout.addWidget(okBtn);
		vboxLayout.setAlignment(okBtn, Qt.AlignmentFlag.AlignRight);
		this.setModal(true);

	}

	private String retrieveQuickQuestVersion() {
		String ver = "unknown";
		try {
			File quickQuestFile = new File(AboutDialog.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if (quickQuestFile.isFile() && quickQuestFile.canRead() && FilenameUtils.isExtension(quickQuestFile.getName(), "jar")) {
				JarFile quickQuestJarFile = new JarFile(quickQuestFile);
				Attributes mainAttrs = quickQuestJarFile.getManifest().getMainAttributes();
				for (Entry<Object, Object> attr : mainAttrs.entrySet()) {
					if (attr.getKey().toString().equals("Implementation-Version")) {
						ver = attr.getValue().toString();
						break;
					}
				}
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ver;
	}
}
