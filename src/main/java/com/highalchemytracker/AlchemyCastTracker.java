package com.highalchemytracker;

final class AlchemyCastTracker
{
	private static final int MAX_PENDING_TICKS = 5;

	private int pendingItemId = -1;
	private int pendingGrossGp;
	private int pendingTick = -1;
	private int lastMagicXp = -1;

	void setMagicXpBaseline(int magicXp)
	{
		lastMagicXp = magicXp;
	}

	void prepareCast(int itemId, int grossGp, int tick)
	{
		if (itemId < 0 || grossGp < 0)
		{
			return;
		}

		pendingItemId = itemId;
		pendingGrossGp = grossGp;
		pendingTick = tick;
	}

	CompletedCast onMagicXpChanged(int magicXp, int tick)
	{
		final int previousXp = lastMagicXp;
		lastMagicXp = magicXp;

		if (pendingItemId < 0 || previousXp >= magicXp || isExpired(tick))
		{
			return null;
		}

		final CompletedCast completedCast = new CompletedCast(pendingItemId, pendingGrossGp);
		clearPendingCast();
		return completedCast;
	}

	void expirePendingCast(int tick)
	{
		if (pendingItemId >= 0 && isExpired(tick))
		{
			clearPendingCast();
		}
	}

	void reset()
	{
		clearPendingCast();
		lastMagicXp = -1;
	}

	private boolean isExpired(int tick)
	{
		return tick < pendingTick || tick - pendingTick > MAX_PENDING_TICKS;
	}

	private void clearPendingCast()
	{
		pendingItemId = -1;
		pendingGrossGp = 0;
		pendingTick = -1;
	}

	static final class CompletedCast
	{
		private final int itemId;
		private final int grossGp;

		private CompletedCast(int itemId, int grossGp)
		{
			this.itemId = itemId;
			this.grossGp = grossGp;
		}

		int getItemId()
		{
			return itemId;
		}

		int getGrossGp()
		{
			return grossGp;
		}
	}
}
