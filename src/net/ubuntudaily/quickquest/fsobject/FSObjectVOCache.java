package net.ubuntudaily.quickquest.fsobject;

import java.util.concurrent.ConcurrentHashMap;

public class FSObjectVOCache {

	private ConcurrentHashMap<Integer, FSObjectVO> cache = new ConcurrentHashMap<Integer, FSObjectVO>();
	public static boolean build(){
		return true;
	}
	public static boolean destroy(){
		return true;
	}
	
}
