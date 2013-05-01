package net.ubuntudaily.quickquest.fsobject;

import java.util.concurrent.BlockingQueue;

import net.ubuntudaily.quickquest.HyperSQLManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.gui.QTableView;

public class ViewModelNoticeHandler implements Runnable {
	private static final Logger LOG=LoggerFactory.getLogger(ViewModelNoticeHandler.class);
	private QTableView view;
	private FSObjectTableModel model;
	private BlockingQueue<ViewModelNotice> readInQueue;

	public ViewModelNoticeHandler(QTableView view, FSObjectTableModel model,
			BlockingQueue<ViewModelNotice> readInQueue) {
		super();
		this.view = view;
		this.model = model;
		this.readInQueue = readInQueue;
	}

	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()){
			ViewModelNotice notice = null;
			try {
				LOG.debug("start taking a notice");
				notice = this.readInQueue.take();
				LOG.debug("finish taking a notice:{}", notice.toString());
			} catch (InterruptedException e) {
				LOG.debug(e.getMessage());
				Thread.currentThread().interrupt();
			}
			
			if(notice != null){
				if(FileOperationType.MODIFY.equals(notice.getType())){
					if(!FileOperationFlowController.modifyAllowed(notice.getFSObject())){
						continue;
					}
				}
				
				if(HyperSQLManager.match(notice.getFSObject().getName(), model.getCriterion())){
					if(FileOperationType.CREATE.equals(notice.getType())){
						
						if(!FileOperationFlowController.createAllowed()){
							continue;
						}
					}
					new Thread(new Runnable(){

						@Override
						public void run() {
							
							//QRect boundingRect = view.visibleRegion().boundingRect();
							//int firstVisibleRow = view.rowAt(boundingRect.top());
							//int lastVisibleRow = view.rowAt(boundingRect.bottom());
							//LOG.debug("firstVisibleRow:{}, lastVisibleRow:{}", firstVisibleRow, lastVisibleRow);
							
							
							//TODO: 当监控到文件变化时,只有当变化的文件在当前的可视区域中时,才需要从cache中删除条目,并通知更新
							model.resetAndClearModel();
							//view.reset();
							
						}
						
					}).start();
				}


			}
		}

	}

}
