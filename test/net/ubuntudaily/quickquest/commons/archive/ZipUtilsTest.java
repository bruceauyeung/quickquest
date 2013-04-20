package net.ubuntudaily.quickquest.commons.archive;

import java.io.File;
import java.nio.charset.Charset;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ZipUtilsTest {

	@Test
	public void detectZipInternalFileNameCharsetTest(){
		final File testFile1 = new File("D:\\Movies\\斯巴达克斯：诅咒者之战\\YYeTs_388da20b4a99a0d42e06eb3fec6ba9cd.zip");
		final Charset actualCharset = ZipUtils.detectZipInternalFileNameCharset(testFile1);
		Assert.assertEquals(actualCharset, Charset.forName("GBK"));
	}

}
