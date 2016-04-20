package com.gosun.hbaseindexer.mapper;

import java.util.Collection;

import org.apache.phoenix.schema.types.PDecimal;

import com.google.common.collect.ImmutableList;
import com.ngdata.hbaseindexer.parse.ByteArrayValueMapper;

/**
 * hbase-indexer对phoenix decimal数据类型的映射支持
 * 
 * @author CrazyPig
 * @since 2016-04-20
 *
 */
public class PhoenixDecimalMapper implements ByteArrayValueMapper {

	public Collection<? extends Object> map(byte[] input) {
		return ImmutableList.of(PDecimal.INSTANCE.toObject(input));
	}

}
