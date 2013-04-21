package net.ubuntudaily.quickquest.commons.io;

import java.io.File;
import java.util.List;

import net.ubuntudaily.quickquest.commons.collections.Lists;



public class FilenameUtils extends org.apache.commons.io.FilenameUtils {
	public static List<String> split(String filename){
		List<String> splited = Lists.newArrayList();
		String normalized = normalize(filename);
		File file = new File(normalized);
		splited.add(0, FileUtils.getNonEmptyName(file));
		File parent = file.getParentFile();
		while(parent != null){
			
			splited.add(0, FileUtils.getNonEmptyName(parent));
			parent = parent.getParentFile();
		}
		return splited;
	}
}
