package net.ubuntudaily.quickquest.commons.io;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FilenameUtilsTests {

	@Test
	public void splitTest(){
		List<String> splitted = FilenameUtils.split("/usr/bin/pwd");
		Assert.assertEquals(splitted.get(0), "/");
		Assert.assertEquals(splitted.get(1), "usr");
		Assert.assertEquals(splitted.get(2), "bin");
		Assert.assertEquals(splitted.get(3), "pwd");
		
	}
}
