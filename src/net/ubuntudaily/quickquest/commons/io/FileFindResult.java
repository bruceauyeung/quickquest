package net.ubuntudaily.quickquest.commons.io;

import java.io.File;

public class FileFindResult {

	private boolean fullScanFinished;
	private int founds;
	private File startDirectory;
	public FileFindResult(File startDirectory,boolean fullScanFinished, int founds) {
		super();
		this.founds = founds;
		this.startDirectory = startDirectory;
		this.fullScanFinished = fullScanFinished;
	}
	public int getFounds() {
		return founds;
	}
	public File getStartDirectory() {
		return startDirectory;
	}
	public boolean isFullScanFinished() {
		return fullScanFinished;
	}
}
