package com.gosun.hbaseindexer.uniquekey;

import org.apache.hadoop.hbase.KeyValue;

import com.google.common.base.Joiner;
import com.ngdata.hbaseindexer.uniquekey.StringUniqueKeyFormatter;

/**
 * 
 * Phoenix Salted Table row key 格式化为 string 类型的格式化类
 * 
 * @author CrazyPig
 * @since 2016-04-25
 *
 */
public class PhoenixSaltedUniqueKeyFormatter extends StringUniqueKeyFormatter {
	
	private static final char SEPARATOR = '-';
	private static final Joiner JOINER = Joiner.on(SEPARATOR);

	@Override
	public String formatRow(byte[] row) {
		
		/*
		 * Phoenix Salted Tables会在row key byte数组的第一个位置插入一个随机byte
		 * 因此在还原成字符串需要去除第一个字符
		 * 
		 */
		byte[] realRow = new byte[row.length - 1];
		System.arraycopy(row, 1, realRow, 0, realRow.length);
		String realRowKey = super.formatRow(realRow);
		return realRowKey;
	}

	@Override
	public String formatKeyValue(KeyValue keyValue) {
		
		/*
		 * Phoenix Salted Tables会在row key byte数组的第一个位置插入一个随机byte
		 * 因此在还原成字符串需要去除第一个字符
		 * 
		 */
		byte[] row = keyValue.getRow();
		byte[] realRow = new byte[row.length - 1];
		System.arraycopy(row, 1, realRow, 0, realRow.length);
		// 参考BaseUniqueKeyFormatter的格式返回
		return JOINER.join(encodeAsString(realRow), encodeAsString(keyValue.getFamily()),
                encodeAsString(keyValue.getQualifier()));
	}

}
