package com.highalchemytracker;

final class AlchemyRecord
{
	private long casts;
	private long grossGp;

	AlchemyRecord()
	{
	}

	AlchemyRecord(long casts, long grossGp)
	{
		this.casts = casts;
		this.grossGp = grossGp;
	}

	long getCasts()
	{
		return casts;
	}

	long getGrossGp()
	{
		return grossGp;
	}

	void addCast(long gp)
	{
		casts++;
		grossGp += gp;
	}
}
