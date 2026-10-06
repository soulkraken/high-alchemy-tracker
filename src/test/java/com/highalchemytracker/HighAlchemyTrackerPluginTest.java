package com.highalchemytracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HighAlchemyTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HighAlchemyTrackerPlugin.class);
		RuneLite.main(args);
	}
}
