package net.ubuntudaily.quickquest.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import net.ubuntudaily.quickquest.HyperSQLManager;
import net.ubuntudaily.quickquest.commons.io.FileUtils;
import net.ubuntudaily.quickquest.fsobject.FSObject;
import net.ubuntudaily.quickquest.fsobject.FSObjectType;
import net.ubuntudaily.quickquest.fsobject.FSObjectVO;

public class FSObjectUtils {

	public static FSObject convertFrom(File file) {
		FSObject obj = new FSObject();
		obj.setDepth(FileUtils.calculateFileDepth(file));
		obj.setLmts(new Timestamp(file.lastModified()));
		obj.setName(FileUtils.getNonEmptyName(file));
		obj.setSize(file.length());
		obj.setType(getType(file));

		return obj;
	}

	public static byte getType(File f) {

		try {
			if (f.isDirectory()) {
				return FSObjectType.DIR;
			} else if (org.apache.commons.io.FileUtils.isSymlink(f)) {
				return FSObjectType.SYLK;
			} else if (f.isFile()) {
				return FSObjectType.FILE;
			} else {
				return FSObjectType.SHLK;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FSObjectType.FILE;
	}

	public static List<FSObjectVO> transform(List<FSObject> all) {
		List<FSObjectVO> voList = new ArrayList<>();
		for (FSObject objInfo : all) {
			FSObjectVO vo = new FSObjectVO();
			vo.setName(objInfo.getName());
			vo.setPath(HyperSQLManager.findParentPath(objInfo));
			vo.setSize(objInfo.getSize());
			vo.setLtms(objInfo.getLmts());
			vo.setFSObject(objInfo);
			vo.setPoid(objInfo.getPoid());
			vo.setRowNum(objInfo.getRowNum());
			voList.add(vo);
	
		}
		return voList;
	}
}
