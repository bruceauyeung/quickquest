package us.suselinks.quickquest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.suselinks.quickquest.Main;
import us.suselinks.quickquest.fsobject.FSObjectTableModel;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QTableView;

public class FSObjectTableModelWorker implements Runnable {
	private Logger LOG = LoggerFactory.getLogger(FSObjectTableModelWorker.class);
	private String criterion;
	private Main mainWindow;
	
	public FSObjectTableModelWorker(Main mainWindow, String criterion) {
		this.mainWindow = mainWindow;
		this.criterion = criterion;
	}
	@Override
	public void run() {
		final FSObjectTableModel model = mainWindow.getTableModel();
		model.resetCriterion(criterion);
		model.prepareData();
		
		final QTableView view = mainWindow.getTableView();
		
		QApplication.invokeLater(new Runnable(){

			@Override
			public void run() {
				
				
				model.resetModel();
				
				//vsb.setRange(1, model.getTotalRowNum().get());
				//view.reset();
				//QScrollBar vsb = view.verticalScrollBar();
				//LOGGER.debug("vertical scrollbar range:{},{}", vsb.minimum(), vsb.maximum());
				//view.update();
				//view.verticalScrollBar().setMinimum(1);
				//LOGGER.debug("model total size:{}",model.getTotalRowNum().get());
				//vsb.setMaximum(model.getTotalRowNum().get() - 1);
				//view.verticalScrollBarPolicy().
				//view.update();
				//view.scrollToTop();
			}
			
		});

	}

}
