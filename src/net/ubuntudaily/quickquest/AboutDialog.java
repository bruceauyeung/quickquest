package net.ubuntudaily.quickquest;


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
		
		//To fit any widget into its parent widget, you need to use a particular layout (grid, horizontal, vertical etc), this conclusion need to be verified. 
		// to make QTabWidget auto-resize when QDialog is resized.
		tabWidget.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding);
		QScrollArea scrollArea = new QScrollArea(this);
		tabWidget.addTab(scrollArea, "General");
		scrollArea.setHorizontalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOff);
		scrollArea.setVerticalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOn);
        //scrollArea.setBackgroundRole(com.trolltech.qt.gui.QPalette.ColorRole.Dark);
        QLabel label = new QLabel(this);
        label.setText(tr("<b>" + tr(parent.AppName)+"</b> &copy;bruce oy<br/>" + "(<a href=\"mailto:bruce.oy@gmail.com\">bruce.oy@gmail.com</a>)" +
        		"<p><b>Version : 0.1</b>" +
        		"<p>Visit my website to get update :<br/>"+
        		"<a href=\"http://www.ubuntudaily.net\">http://www.ubuntudaily.net</a>"+
        		"<p>This program intends to help you to  find files or directories as fast as lightning"));
        label.setWordWrap(true);
        label.setMargin(5);
        label.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding);
       scrollArea.setWidget(label);
        
		
		vboxLayout.addWidget(tabWidget);
		//vboxLayout.setAlignment(tabWidget, Qt.AlignmentFlag.AlignCenter);
		QPushButton okBtn = new QPushButton("OK",this);
		okBtn.clicked.connect(this, "accept()");
		okBtn.setSizePolicy(QSizePolicy.Policy.Fixed, QSizePolicy.Policy.Fixed);
		vboxLayout.addWidget(okBtn);
		vboxLayout.setAlignment(okBtn, Qt.AlignmentFlag.AlignRight);
		this.setModal(true);
		
		
	}
}
