package net.ubuntudaily.quickquest.commons.io;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.ubuntudaily.quickquest.commons.collections.Lists;
import net.ubuntudaily.quickquest.commons.lang.OSValidator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

	public static boolean OpenWithDefAppPureJava(File file) {

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
		List<String> cmd = Lists.newArrayList();
		cmd.add("xdg-open");
		String openArg = file.getName();
		cmd.add(openArg);

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(file.getParentFile());
		boolean succ = true;
		try {

			// java just start a process and then run the following codes as
			// needed.
			Process proc = pb.start();
			final InputStream errorStream = proc.getErrorStream();
			if (errorStream != null) {
				String err = IOUtils.toString(errorStream);
				if (StringUtils.isNotEmpty(err)) {
					succ = false;
				}

			}
		} catch (IOException e) {
			LOG.debug(e.getMessage());
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
		if (!FileUtils.OpenWithDefAppPureJava(file)) {

			if (OSValidator.isUnix()) {

				if (!FileUtils.openWithDefAppXdgOpen(file)) {
					// TODO: tell user this file can not be opened.
					succ = false;
				}
			} else if (OSValidator.isWindows()) {

				succ = false;

			} else if (OSValidator.isMac()) {
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
}
