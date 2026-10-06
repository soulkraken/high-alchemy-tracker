package com.highalchemytracker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AlchemyRecordCodecTest
{
	@Test
	public void roundTripsRecord()
	{
		final AlchemyRecord decoded = AlchemyRecordCodec.decode(
			AlchemyRecordCodec.encode(new AlchemyRecord(42, 123_456)));

		assertEquals(42, decoded.getCasts());
		assertEquals(123_456, decoded.getGrossGp());
	}

	@Test
	public void rejectsUnknownVersion()
	{
		assertNull(AlchemyRecordCodec.decode("2:42:123456"));
	}

	@Test
	public void rejectsNegativeValues()
	{
		assertNull(AlchemyRecordCodec.decode("1:-1:100"));
		assertNull(AlchemyRecordCodec.decode("1:1:-100"));
	}

	@Test
	public void rejectsMalformedValue()
	{
		assertNull(AlchemyRecordCodec.decode(null));
		assertNull(AlchemyRecordCodec.decode("not-a-record"));
		assertNull(AlchemyRecordCodec.decode("1:casts:gp"));
	}
}
