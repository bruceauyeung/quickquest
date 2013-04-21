package net.ubuntudaily.quickquest.fsobject;

import java.util.ArrayList;
import java.util.List;

import com.trolltech.qt.QtBlockedSlot;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QAbstractTableModel;

public class FastFSObjectTableModel extends QAbstractTableModel {

	private List<FSObjectVO> voList = null;
	public FastFSObjectTableModel(List<FSObjectVO> voLst) {
		this.voList = new ArrayList<>(voLst.size());
		for(FSObjectVO vo :voLst){
			voList.add(vo);
		}
	}
	@Override
	@QtBlockedSlot
	public int columnCount(QModelIndex arg0) {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	@QtBlockedSlot
	public Object data(QModelIndex modelIndex, int role) {
		
		if(role == Qt.ItemDataRole.DisplayRole){
			int row = modelIndex.row();
			FSObjectVO vo = voList.get(row);
			switch(modelIndex.column()){
			case 0: return vo.getName();
			case 1: return vo.getPath();
			case 2: return vo.getSize();
			case 3: return vo.getLtms().toString();
			}
		}
		// TODO Auto-generated method stub
		// return new FSObjectVO("bruce", "test", 123, new
		// Timestamp(System.currentTimeMillis()));

		return null;

	}

	@Override
	@QtBlockedSlot
	public int rowCount(QModelIndex arg0) {
		// TODO Auto-generated method stub
		return voList.size();
	}

	@Override
	@QtBlockedSlot
	public Object headerData(int section, Orientation orientation, int role) {
		
		 if (role == Qt.ItemDataRole.DisplayRole){
				if(orientation.equals(Qt.Orientation.Horizontal)){
					
					switch (section) {
					case 0:
						return "Name";
					case 1:
						return "Path";
					case 2:
						return "Size";
					case 3:
						return "LastModifiedTime";
					}
				}
		 }

		return super.headerData(section, orientation, role);
	}

	@Override
	@QtBlockedSlot
	public boolean setData(QModelIndex index, Object value, int role) {
		
		if(role == Qt.ItemDataRole.CheckStateRole){
			QAbstractItemModel model = index.model();
		}
		return super.setData(index, value, role);
	}
	
	@Override
	@QtBlockedSlot
	public boolean canFetchMore(QModelIndex parent) {
		// TODO Auto-generated method stub
		return super.canFetchMore(parent);
	}
	@Override
	@QtBlockedSlot
	public void fetchMore(QModelIndex parent) {
		// TODO Auto-generated method stub
		super.fetchMore(parent);
	}
	
}
