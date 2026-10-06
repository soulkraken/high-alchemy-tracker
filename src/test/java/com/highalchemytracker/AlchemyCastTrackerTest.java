package com.highalchemytracker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class AlchemyCastTrackerTest
{
	@Test
	public void recordsPreparedCastOnMagicXpGain()
	{
		final AlchemyCastTracker tracker = new AlchemyCastTracker();
		tracker.setMagicXpBaseline(10_000);
		tracker.prepareCast(2, 120, 50);

		final AlchemyCastTracker.CompletedCast cast = tracker.onMagicXpChanged(10_065, 51);

		assertNotNull(cast);
		assertEquals(2, cast.getItemId());
		assertEquals(120, cast.getGrossGp());
	}

	@Test
	public void ignoresPreparedCastWithoutXpGain()
	{
		final AlchemyCastTracker tracker = new AlchemyCastTracker();
		tracker.setMagicXpBaseline(10_000);
		tracker.prepareCast(2, 120, 50);

		assertNull(tracker.onMagicXpChanged(10_000, 51));
	}

	@Test
	public void doesNotCountOneCastTwice()
	{
		final AlchemyCastTracker tracker = new AlchemyCastTracker();
		tracker.setMagicXpBaseline(10_000);
		tracker.prepareCast(2, 120, 50);

		assertNotNull(tracker.onMagicXpChanged(10_065, 51));
		assertNull(tracker.onMagicXpChanged(10_130, 52));
	}

	@Test
	public void expiresOldPreparedCast()
	{
		final AlchemyCastTracker tracker = new AlchemyCastTracker();
		tracker.setMagicXpBaseline(10_000);
		tracker.prepareCast(2, 120, 50);
		tracker.expirePendingCast(56);

		assertNull(tracker.onMagicXpChanged(10_065, 56));
	}

	@Test
	public void acceptsExperienceMultipliers()
	{
		final AlchemyCastTracker tracker = new AlchemyCastTracker();
		tracker.setMagicXpBaseline(10_000);
		tracker.prepareCast(2, 120, 50);

		assertNotNull(tracker.onMagicXpChanged(10_325, 51));
	}
}
