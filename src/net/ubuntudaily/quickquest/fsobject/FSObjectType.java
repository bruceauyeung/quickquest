package net.ubuntudaily.quickquest.fsobject;

public class FSObjectType {

	/**
	 * file
	 */
	public static final byte FILE = 0;
	
	/**
	 * directory
	 */
	public static final byte DIR = 1;
	
	/**
	 * symbol link, used only in linux 
	 */
	public static final byte SYLK = 2;
	
	/**
	 * shell link, also known as shortcut, used only in windows.
	 */
	public static final byte SHLK = 3;
	
	
}
