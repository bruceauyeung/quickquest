package us.suselinks.quickquest.commons.json;

import java.io.File;

import org.testng.annotations.Test;

import us.suselinks.quickquest.commons.json.JsonHelper;
import us.suselinks.quickquest.preferences.MonitoredDirectory;
import us.suselinks.quickquest.preferences.Preferences;

public class JsonHelperTest {

	@Test
	public void toJsonTest(){
		Preferences prefs = new Preferences();
		MonitoredDirectory moniDir = new MonitoredDirectory(new File("c\\"), true, true);
		prefs.getDirectoryTab().addMonitoredDirectory(moniDir);
		System.out.println(JsonHelper.toJson(prefs));
	}
}
