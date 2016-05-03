package com.gosun.hbaseindexer.uniquekey;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author CrazyPig
 * @since 2016-04-25
 *
 */
public class PhoenixSaltedUniqueKeyFormatterTest {

	@Test
	public void testFormat() {
		PhoenixSaltedUniqueKeyFormatter formatter = new PhoenixSaltedUniqueKeyFormatter();
		byte[] bytes = null;
		try {
			bytes = "abc".getBytes("UTF-8");
			byte[] newBytes = new byte[bytes.length + 1];
			newBytes[0] = 1;
			System.arraycopy(bytes, 0, newBytes, 1, bytes.length);
			Assert.assertEquals("abc", formatter.formatRow(newBytes));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
