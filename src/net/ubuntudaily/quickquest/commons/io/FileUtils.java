package net.ubuntudaily.quickquest.commons.io;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import net.ubuntudaily.quickquest.commons.collections.Maps;
import net.ubuntudaily.quickquest.commons.lang.OSValidator;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils extends org.apache.commons.io.FileUtils {

	private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
	private static final File[] AVAIL_FS_ROOTS = File.listRoots();

	/**
	 * &#8725;home&#8725;bruce&#8725; 的深度是 2。 &#8725;的深度是0。
	 * 
	 * @param file
	 * @return
	 */
	public static int calculateFileDepth(File file) {
		int depth = 0;
		File pfile = file.getParentFile();
		while (pfile != null) {
			depth++;
			pfile = pfile.getParentFile();
		}
		return depth;
	}

	public static String getNonEmptyName(File file) {

		// when current directory is root directory in unix-like system, just
		// add a slash.
		if (file.getName().isEmpty()) {
			if (file.getAbsolutePath().equals("/")) {
				return "/";
			} else if (isFileSystemRoot(file)) {
				return file.getAbsolutePath();
			}
		}
		return file.getName();
	}

	public static final boolean isFileSystemRoot(File file) {
		for (File f : AVAIL_FS_ROOTS) {
			if (f.equals(file)) {
				return true;
			}
		}
		return false;
	}

	public static boolean openWithDefAppWin(File file){
		if(!file.exists()){
			return false;
		}
		boolean succ = true;
		CommandLine cmdLine = new CommandLine("cmd");
		cmdLine.addArgument("/c");
		cmdLine.addArgument("start");
		cmdLine.addArgument("${file}");
		Map<String, File> map = Maps.newHashMap();
		map.put("file", file);
		cmdLine.setSubstitutionMap(map);

		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler());
		try {
			executor.execute(cmdLine, resultHandler);
		} catch (ExecuteException e1) {
			LOG.error(e1.getMessage());
			succ = false;
		} catch (IOException e1) {
			LOG.error(e1.getMessage());
			succ = false;
		}
		

		return succ;
	}
	/**
	 * under my openSUSE12.3 system, this method can not handle directories correctly. 
	 * for example, qmmp program will be launched when open a directory using this method.
	 * under my windows 7 system, this method crashes when opening a directory, what a shame !
	 * @deprecated
	 * @param file
	 * @return
	 */
	public static boolean openWithDefAppPureJava(File file) {

		if(!file.exists()){
			return false;
		}
		File toOpen = file;
		boolean succ = false;
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN)) {
				try {

					desktop.open(toOpen);
					succ = true;
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			}
		}
		return succ;

	}

	public static boolean openWithDefAppXdgOpen(File file) {
		
		if(!file.exists()){
			return false;
		}
		boolean succ = true;
		CommandLine cmdLine = new CommandLine("xdg-open");
		cmdLine.addArgument("${file}");
		Map<String, File> map = Maps.newHashMap();
		map.put("file", file);
		cmdLine.setSubstitutionMap(map);

		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler());
		try {
			executor.execute(cmdLine, resultHandler);
		} catch (ExecuteException e1) {
			LOG.error(e1.getMessage());
			succ = false;
		} catch (IOException e1) {
			LOG.error(e1.getMessage());
			succ = false;
		}
		

		return succ;
	}

	/**
	 * in windows pls refer to this website:
	 * http://frank.zinepal.com/open-a-file-in-the-default-application-using
	 */
	public static final boolean openWithDefApp(File file) {
		boolean succ = true;
		
		if (OSValidator.isUnix()) {

			if (!FileUtils.openWithDefAppXdgOpen(file)) {
				// TODO: tell user this file can not be opened.
				succ = false;
			}
		} else if (OSValidator.isMac()) {

			if (!FileUtils.openWithDefAppPureJava(file)) {
				succ = false;
			}

		} 
		else if(OSValidator.isWindows()){
			if (!FileUtils.openWithDefAppWin(file)) {
				succ = false;
			}
		}
		return succ;
	}

	public static boolean mkdirQuietly(File destDir) {
		if (destDir != null) {
			if (!destDir.exists()) {
				
				destDir.mkdirs();
				return true;
			}else{
				if(destDir.isDirectory()){
					return true;
				}
			}
		}
		return false;
	}
	
	public static final File fromURL(URL url){
		File f = null;
		
		try {
			f = new File(url.toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
}
