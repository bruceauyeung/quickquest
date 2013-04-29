package net.ubuntudaily.quickquest.commons.collections;

import java.util.HashMap;

import org.apache.commons.lang3.Validate;

public class Maps {
	  public static <K, V> HashMap<K, V> newHashMap()
	  {
	    return new HashMap();
	  }

	  public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize)
	  {
	    return new HashMap(capacity(expectedSize));
	  }

	  static int capacity(int expectedSize)
	  {
	    if (expectedSize < 3) {
	      Validate.isTrue(expectedSize >= 0);
	      return expectedSize + 1;
	    }
	    if (expectedSize < 1073741824) {
	      return expectedSize + expectedSize / 3;
	    }
	    return 2147483647;
	  }
}
