package net.ubuntudaily.quickquest.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.ubuntudaily.quickquest.QuickQuest;

public class DirectoryTab {
	
	File databaseLocation = QuickQuest.QUICK_QUEST_CFG_DIR;
	File oldDatabaseLocation = null;
	List<MonitoredDirectory> monitoredDirectories = new ArrayList<MonitoredDirectory>();
	public File getDatabaseLocation() {
		return databaseLocation;
	}
	public void setDatabaseLocation(File databaseLocation) {
		this.databaseLocation = databaseLocation;
	}
	public List<MonitoredDirectory> getMonitoredDirectories() {
		return monitoredDirectories;
	}
	public void setMonitoredDirectories(
			List<MonitoredDirectory> monitoredDirectories) {
		this.monitoredDirectories = monitoredDirectories;
	}
	
	public void addMonitoredDirectory(MonitoredDirectory dir){
		monitoredDirectories.add(dir);
	}
	public File getOldDatabaseLocation() {
		return oldDatabaseLocation;
	}
	public void setOldDatabaseLocation(File oldDatabaseLocation) {
		this.oldDatabaseLocation = oldDatabaseLocation;
	}
}
