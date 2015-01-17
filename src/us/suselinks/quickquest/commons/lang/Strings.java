package us.suselinks.quickquest.commons.lang;

public class Strings {

	public static final boolean containsGarbledCharacter(String s){
		
		for(int i = 0; i < s.length(); i++){
			if(Characters.isGarbledChar(s.charAt(i))){
				return true;
			}
		}
		return false;
	}
}
