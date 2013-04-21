package net.ubuntudaily.quickquest.fsobject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSObjectCache {

	private static Logger LOGGER = LoggerFactory.getLogger(FSObjectCache.class);
	private final ConcurrentMap<Integer, FSObjectVO> cache = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
	private final Range<Integer> rowNumRange;
	public static final int MAX_CACHE_SIZE = 50;

	public FSObjectCache(Range<Integer> rowNumRange) {
//		cache = new ArrayList<FSObjectVO>(rowNumRange.getMaximum()
//				- rowNumRange.getMinimum() + 1);
		cache.clear();
		this.rowNumRange = rowNumRange;
	}

	public FSObjectVO get(int rownum) {
		FSObjectVO fsObjectVO = null;
		try {
//			fsObjectVO = cache.get(rownum - rowNumRange.getMinimum());
			fsObjectVO = cache.get(rownum);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		}
		return fsObjectVO;
	}
	public boolean remove(int rownum){
//		cache.remove(rownum - rowNumRange.getMinimum());
		cache.remove(rownum);
		return true;
	}
	public boolean remove(int rownum, int count){
		int startRowNum = rownum;
		int endRowNum = rownum + count - 1;
		for(int row = startRowNum; row <=  endRowNum; row++){
			remove(row);
		}
		return true;
	}

	public void copyFrom(List<FSObjectVO> src) {

		int total = rowNumRange.getMaximum() - rowNumRange.getMinimum() +1;
		int start = rowNumRange.getMinimum();
		// int toCopyNum = src.size();
		try {
			for (int i = 0; i < total; i++) {
				cache.put(start + i,src.get(i));
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		}
	}

	public Range<Integer> getRowNumRange() {
		return rowNumRange;
	}
	public FSObjectVO get(FSObject fso){
		for(FSObjectVO vo :cache.values()){
			if(vo.getFSObject().equals(fso)){
				return vo;
			}
		}
		return null;
	}
}
