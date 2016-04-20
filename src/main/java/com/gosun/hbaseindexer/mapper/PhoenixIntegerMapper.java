package com.gosun.hbaseindexer.mapper;

import java.util.Collection;

import org.apache.phoenix.schema.types.PInteger;

import com.google.common.collect.ImmutableList;
import com.ngdata.hbaseindexer.parse.ByteArrayValueMapper;

/**
 * hbase-indexer对phoenix integer数据类型的映射支持
 * 
 * @author CrazyPig
 * @since 2016-04-20
 *
 */
public class PhoenixIntegerMapper implements ByteArrayValueMapper {

	public Collection<? extends Object> map(byte[] input) {
		return ImmutableList.of(PInteger.INSTANCE.toObject(input));
	}

}
