package net.ubuntudaily.quickquest.fsobject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.ubuntudaily.quickquest.HyperSQLManager;
import net.ubuntudaily.quickquest.utils.FSObjectUtils;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.QtBlockedSlot;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QAbstractTableModel;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QFileIconProvider;

public class FSObjectTableModel extends QAbstractTableModel {

	private Logger LOGGER = LoggerFactory.getLogger(FSObjectTableModel.class);
	private String criterion;
	private Map<String, String> criteria = null;
	private FSObjectCache cache;
	private AtomicInteger totalRowNum = new AtomicInteger(-1);

	public Signal1<Integer> rowCountChanged = new Signal1<Integer>();
	private final QFileIconProvider fip = new QFileIconProvider();

	public FSObjectTableModel(String criterion) {
		resetCriterion(criterion);
	}

	private void setRowCount(final int rowCount) {
		this.totalRowNum.set(rowCount);
		QApplication.invokeLater(new Runnable() {

			@Override
			public void run() {
				rowCountChanged.emit(rowCount);
			}

		});

	}

	public void resetCriterion(String criterion) {

		this.criterion = criterion;
		this.criteria = new HashMap<String, String>();
		if (StringUtils.isNotBlank(criterion)) {

			criteria.put("name", this.criterion);
		}
		cache = null;
		final Integer total = Integer.valueOf(String.valueOf(HyperSQLManager
				.countLike(this.criteria)));
		LOGGER.debug("resetCriterion recount:{}", total);
		setRowCount(total);
		// this.reset();
	}

	@Override
	@QtBlockedSlot
	public int columnCount(QModelIndex arg0) {
		// logger.debug("columnCount method is called.");
		if (arg0 != null) {

			LOGGER.debug(new StringBuilder("{row:").append(arg0.row())
					.append(", col:").append(arg0.column()).append(")")
					.toString());
		}
		return 4;
	}

	public void prepareData() {
		retrieveData(0);

	}

	/**
	 * a delegate method to
	 * {@link com.trolltech.qt.core.QAbstractItemModel#reset}.
	 * <p>
	 * {@link com.trolltech.qt.core.QAbstractItemModel#reset} method is
	 * <code>protected</code> and <code>final</code>, so can not be called
	 * outside of this package.
	 */
	public void resetModel() {
		this.reset();
	}

	public void resetAndClearModel() {
		resetCriterion(this.criterion);
		resetModel();
	}

	@Override
	@QtBlockedSlot
	public Object data(QModelIndex modelIndex, int role) {
		// logger.debug("data method is called.");
		FSObjectVO vo = null;
		int row = modelIndex.row();

		if (cache == null || !cache.getRowNumRange().contains(row)) {
			retrieveData(row);

		}
		vo = cache.get(row);
		if (vo != null) {

			if (role == Qt.ItemDataRole.DecorationRole) {
				if (modelIndex.column() == 0) {
					QFileInfo fileInfo = new QFileInfo(new File(vo.getPath(),
							vo.getName()).getAbsolutePath());
					return fip.icon(fileInfo);
				}
			} else if (role == Qt.ItemDataRole.DisplayRole) {
				switch (modelIndex.column()) {
				case 0:
					return vo.getName();
				case 1:
					return vo.getPath();
				case 2:
					return vo.getSize();
				case 3:
					return vo.getLtms().toString();

				case 4:
					return vo.getPoid();
				case 5:
					return vo.getRowNum();
				}
			}

		} else {
			return null;
		}

		// TODO Auto-generated method stub
		// return new FSObjectVO("bruce", "test", 123, new
		// Timestamp(System.currentTimeMillis()));

		return null;

	}

	/**
	 * 
	 * @param row
	 *            the row number in QAbstractItemModel, starts with 0
	 */
	private void retrieveData(int row) {
		int startRow = -1;
		int endRow = -1;
		if (cache != null) {
			LOGGER.debug("row:{}, min:{}, max:{}", row, cache.getRowNumRange()
					.getMinimum(), cache.getRowNumRange().getMaximum());
			if (row < cache.getRowNumRange().getMinimum()) {
				startRow = row - FSObjectCache.MAX_CACHE_SIZE + 1;

				endRow = row;
			} else if (row > cache.getRowNumRange().getMaximum()) {
				startRow = row;
				endRow = row + FSObjectCache.MAX_CACHE_SIZE - 1;
			}
		} else {
			startRow = row;
			endRow = row + FSObjectCache.MAX_CACHE_SIZE - 1;
		}
		
		if (startRow < 0) {
			startRow = 0;
		}
		
		Range<Integer> between = Range.between(startRow, endRow);
		long start = System.nanoTime();
		List<FSObject> result = HyperSQLManager.selectLike(this.criteria,
				between);
		LOGGER.debug(" select time(ms) : {}",
				(System.nanoTime() - start) / 1000000);

		if (result.size() < FSObjectCache.MAX_CACHE_SIZE) {
			between = Range.between(startRow, startRow + result.size() - 1);
		}
		cache = new FSObjectCache(between);

		start = System.nanoTime();
		cache.copyFrom(FSObjectUtils.transform(result));
		LOGGER.debug(" transform time(ms):" + (System.nanoTime() - start)
				/ 1000000);
		LOGGER.debug("cache has been built, rownum range : {}, {} ", cache
				.getRowNumRange().getMinimum(), cache.getRowNumRange()
				.getMaximum());
	}

	@Override
	@QtBlockedSlot
	public int rowCount(QModelIndex arg0) {
		// logger.debug("rowCount method is called.");
		if (arg0 != null) {

			LOGGER.debug(new StringBuilder("{row:").append(arg0.row())
					.append(", col:").append(arg0.column()).append(")")
					.toString());
		}
		long start = System.nanoTime();
		if (totalRowNum.get() < 0) {

			setRowCount(Integer.valueOf(String.valueOf(HyperSQLManager
					.countLike(this.criteria))));
			LOGGER.debug("recount :{}", totalRowNum.get());
		}
		// logger.debug(" rowCount time(ms):" + (System.nanoTime() - start)
		// /1000000);
		return totalRowNum.get();
		// return 10;
	}

	@Override
	@QtBlockedSlot
	public Object headerData(int section, Orientation orientation, int role) {
		// logger.debug("headerData method is called.");
		if (role == Qt.ItemDataRole.DisplayRole
				|| role == Qt.ItemDataRole.FontRole
				|| role == Qt.ItemDataRole.TextAlignmentRole) {
			if (orientation.equals(Qt.Orientation.Horizontal)) {

				switch (section) {
				case 0:
					return "Name";
				case 1:
					return "Path";
				case 2:
					return "Size";
				case 3:
					return "Date Modified";
				}
			}
		}

		return super.headerData(section, orientation, role);
	}

	@Override
	@QtBlockedSlot
	public boolean setData(QModelIndex index, Object value, int role) {
		// logger.debug("setData method is called.");
		if (role == Qt.ItemDataRole.CheckStateRole) {
			QAbstractItemModel model = index.model();
		}
		return super.setData(index, value, role);
	}

	@Override
	@QtBlockedSlot
	public boolean canFetchMore(QModelIndex parent) {
		LOGGER.debug("canFetchMore method is called.");
		// if(parent.row() < totalRowNum){
		// return true;
		// }
		// else{
		// return false;
		// }
		final boolean canFetchMore = super.canFetchMore(parent);
		return canFetchMore;
	}

	@Override
	@QtBlockedSlot
	public void fetchMore(QModelIndex parent) {
		LOGGER.debug("fetchMore method is called.");
		super.fetchMore(parent);
	}

	public AtomicInteger getTotalRowNum() {
		return totalRowNum;
	}

	/**
	 * TODO
	 */
	@Override
	@QtBlockedSlot
	public boolean insertRows(int row, int count, QModelIndex parent) {
		// TODO Auto-generated method stub
		return super.insertRows(row, count, parent);
	}

	public boolean removeFSObjectVO(FSObject fso) {
		// TODO
		return true;
	}

	/**
	 * TODO
	 */
	@Override
	@QtBlockedSlot
	public boolean removeRows(int row, int count, QModelIndex parent) {
		beginRemoveRows(parent, row, row + count - 1);
		cache.remove(row, count);
		endRemoveRows();
		return true;
		// return super.removeRows(row, count, parent);
	}

	public FSObjectVO getRow(int rowNum) {
		return cache.get(rowNum);
	}

	public String getCriterion() {
		return criterion;
	}
}
