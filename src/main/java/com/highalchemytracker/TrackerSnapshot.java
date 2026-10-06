package com.highalchemytracker;

import java.util.Collections;
import java.util.List;

final class TrackerSnapshot
{
	private final long lifetimeCasts;
	private final long lifetimeGrossGp;
	private final long sessionCasts;
	private final long sessionGrossGp;
	private final List<ItemRow> items;

	TrackerSnapshot(long lifetimeCasts, long lifetimeGrossGp, long sessionCasts,
		long sessionGrossGp, List<ItemRow> items)
	{
		this.lifetimeCasts = lifetimeCasts;
		this.lifetimeGrossGp = lifetimeGrossGp;
		this.sessionCasts = sessionCasts;
		this.sessionGrossGp = sessionGrossGp;
		this.items = Collections.unmodifiableList(items);
	}

	long getLifetimeCasts()
	{
		return lifetimeCasts;
	}

	long getLifetimeGrossGp()
	{
		return lifetimeGrossGp;
	}

	long getSessionCasts()
	{
		return sessionCasts;
	}

	long getSessionGrossGp()
	{
		return sessionGrossGp;
	}

	List<ItemRow> getItems()
	{
		return items;
	}

	static final class ItemRow
	{
		private final String name;
		private final long casts;
		private final long grossGp;

		ItemRow(String name, long casts, long grossGp)
		{
			this.name = name;
			this.casts = casts;
			this.grossGp = grossGp;
		}

		String getName()
		{
			return name;
		}

		long getCasts()
		{
			return casts;
		}

		long getGrossGp()
		{
			return grossGp;
		}
	}
}
