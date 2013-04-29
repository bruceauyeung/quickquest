package net.ubuntudaily.quickquest.preferences;

import java.io.File;

import net.ubuntudaily.quickquest.QuickQuest;

public class MonitoredDirectory {

	File directory = QuickQuest.USER_HOME;
	boolean inSearchPath = true;
	boolean changesMonitored = true;
	boolean fullScanFinished = false;
	public MonitoredDirectory() {
	}
	public MonitoredDirectory(File directory, boolean inSearchPath,
			boolean changesMonitored) {
		this.directory = directory;
		this.inSearchPath = inSearchPath;
		this.changesMonitored = changesMonitored;
	}

	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public boolean isChangesMonitored() {
		return changesMonitored;
	}
	public void setChangesMonitored(boolean changesMonitored) {
		this.changesMonitored = changesMonitored;
	}
	public boolean isInSearchPath() {
		return inSearchPath;
	}
	public void setInSearchPath(boolean inSearchPath) {
		this.inSearchPath = inSearchPath;
	}
	public boolean isFullScanFinished() {
		return fullScanFinished;
	}
	public void setFullScanFinished(boolean fullScanFinished) {
		this.fullScanFinished = fullScanFinished;
	}
}
