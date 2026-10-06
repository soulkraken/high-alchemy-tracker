package com.highalchemytracker;

final class AlchemyRecordCodec
{
	private static final String VERSION = "1";
	private static final String SEPARATOR = ":";

	private AlchemyRecordCodec()
	{
	}

	static String encode(AlchemyRecord record)
	{
		return VERSION + SEPARATOR + record.getCasts() + SEPARATOR + record.getGrossGp();
	}

	static AlchemyRecord decode(String value)
	{
		if (value == null)
		{
			return null;
		}

		final String[] parts = value.split(SEPARATOR, -1);
		if (parts.length != 3 || !VERSION.equals(parts[0]))
		{
			return null;
		}

		try
		{
			final long casts = Long.parseLong(parts[1]);
			final long grossGp = Long.parseLong(parts[2]);
			if (casts < 0 || grossGp < 0)
			{
				return null;
			}
			return new AlchemyRecord(casts, grossGp);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}
}
