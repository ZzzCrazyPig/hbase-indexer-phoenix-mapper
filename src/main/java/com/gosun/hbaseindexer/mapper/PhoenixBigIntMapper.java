package com.gosun.hbaseindexer.mapper;

import java.util.Collection;

import org.apache.phoenix.schema.types.PLong;

import com.google.common.collect.ImmutableList;
import com.ngdata.hbaseindexer.parse.ByteArrayValueMapper;

/**
 * hbase-indexer对phoenix bigint数据类型的映射支持
 * 
 * @author CrazyPig
 * @since 2016-04-20
 *
 */
public class PhoenixBigIntMapper implements ByteArrayValueMapper {

	public Collection<? extends Object> map(byte[] input) {
		return ImmutableList.of(PLong.INSTANCE.toObject(input));
	}
	
}
