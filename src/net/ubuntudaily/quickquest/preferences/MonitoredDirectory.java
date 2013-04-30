package net.ubuntudaily.quickquest.preferences;

import java.io.File;

import net.ubuntudaily.quickquest.QuickQuest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.changesMonitored)
				.append(this.fullScanFinished).append(this.inSearchPath)
				.append(this.directory).build();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitoredDirectory other = (MonitoredDirectory) obj;
		return new EqualsBuilder()
				.append(this.changesMonitored, other.changesMonitored)
				.append(this.fullScanFinished, other.fullScanFinished)
				.append(this.inSearchPath, other.inSearchPath)
				.append(this.directory, other.directory).build();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
