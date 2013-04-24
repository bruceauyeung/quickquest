package net.ubuntudaily.quickquest;

import java.io.File;
import java.io.IOException;

import net.ubuntudaily.quickquest.commons.json.JsonHelper;
import net.ubuntudaily.quickquest.preferences.Preferences;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QuickQuest {

	private static final Logger LOG = LoggerFactory.getLogger(QuickQuest.class);
	public static final File USER_HOME = new File(System.getProperty("user.home"));
	public static final File QUICK_QUEST_CFG_DIR = new File(USER_HOME, ".quickquest");
	private static Preferences prefs = new Preferences();
	
	public static final void loadPreferences(){
		final File jsonFile = new File(QuickQuest.QUICK_QUEST_CFG_DIR, "prefereces.json");
		if(jsonFile.exists()){
			prefs = JsonHelper.fromJson(jsonFile, Preferences.class);
		}
		
		final File oldDbLoc = prefs.getDirectoryTab().getOldDatabaseLocation();
		final File dbLoc = prefs.getDirectoryTab().getDatabaseLocation();

		if(oldDbLoc != null && oldDbLoc.isDirectory()){
			
			if(dbLoc !=null){
				if(net.ubuntudaily.quickquest.commons.io.FileUtils.mkdirQuietly(dbLoc) && !oldDbLoc.equals(dbLoc)){
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
}
