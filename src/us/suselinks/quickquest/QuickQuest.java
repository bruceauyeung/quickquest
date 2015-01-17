package us.suselinks.quickquest;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.suselinks.quickquest.commons.json.JsonHelper;
import us.suselinks.quickquest.preferences.Preferences;


public class QuickQuest {

	private static final Logger LOG = LoggerFactory.getLogger(QuickQuest.class);
	public static final File USER_HOME = new File(System.getProperty("user.home"));
	public static final File QUICK_QUEST_PROG_DIR=initQuickQuestProgDir();
	public static final File QUICK_QUEST_CFG_DIR = new File(USER_HOME, ".quickquest");
	private static Preferences prefs = new Preferences();
	
	private static final File initQuickQuestProgDir(){
		File f = null;
		
		String progPath = System.getProperty("quickquest.prog.dir");
		if(StringUtils.isEmpty(progPath)){
			
			// quickquest is launched not by quickquest.sh
			URL parentURL = QuickQuest.class.getResource("/");
			if(parentURL == null){
				LOG.debug("parentURL is null");
			}
			
			f = us.suselinks.quickquest.commons.io.FileUtils.fromURL(parentURL);
		}else{
			
			// quickquest is launched by quickquest.sh
			f = new File(progPath);
		}

		return f;
	}
	public static final void loadPreferences(){
		final File jsonFile = new File(QuickQuest.QUICK_QUEST_CFG_DIR, "prefereces.json");
		if(jsonFile.exists()){
			prefs = JsonHelper.fromJson(jsonFile, Preferences.class);
		}
		
		final File oldDbLoc = prefs.getDirectoryTab().getOldDatabaseLocation();
		final File dbLoc = prefs.getDirectoryTab().getDatabaseLocation();

		if(oldDbLoc != null && oldDbLoc.isDirectory()){
			
			if(dbLoc !=null){
				if(us.suselinks.quickquest.commons.io.FileUtils.mkdirQuietly(dbLoc) && !oldDbLoc.equals(dbLoc)){
					try {
						File quickQuestProperties = new File(oldDbLoc, "quickquest.properties");
						if(quickQuestProperties.isFile()){
							FileUtils.moveFileToDirectory(quickQuestProperties, dbLoc, true);
						}

						File quickQuestScript = new File(oldDbLoc, "quickquest.script");
						if(quickQuestScript.isFile()){
							FileUtils.moveFileToDirectory(quickQuestScript, dbLoc, true);
						}
						
						prefs.getDirectoryTab().setOldDatabaseLocation(null);
					} catch (IOException e) {
						LOG.error(e.getMessage());
					}
				}

				
			}
		}
	}
	public static final Preferences getPreferences(){
		return prefs;
	}
	
	public static final void savePreferencesToDisk(){
		final File jsonFile = new File(QuickQuest.QUICK_QUEST_CFG_DIR, "prefereces.json");
		JsonHelper.toJson(jsonFile, prefs);
	}
}
