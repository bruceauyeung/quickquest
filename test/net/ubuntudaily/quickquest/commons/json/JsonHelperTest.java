package net.ubuntudaily.quickquest.commons.json;

import java.io.File;

import net.ubuntudaily.quickquest.preferences.MonitoredDirectory;
import net.ubuntudaily.quickquest.preferences.Preferences;

import org.testng.annotations.Test;

public class JsonHelperTest {

	@Test
	public void toJsonTest(){
		Preferences prefs = new Preferences();
		MonitoredDirectory moniDir = new MonitoredDirectory(new File("c\\"), true, true);
		prefs.getDirectoryTab().addMonitoredDirectory(moniDir);
		System.out.println(JsonHelper.toJson(prefs));
	}
}
