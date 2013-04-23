package net.ubuntudaily.quickquest;

import java.io.File;

public class QuickQuest {

	public static final File USER_HOME = new File(System.getProperty("user.home"));
	public static File QUICK_QUEST_DATA_DIR = new File(USER_HOME, ".quickquest");

}
