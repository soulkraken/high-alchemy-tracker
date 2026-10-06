package com.highalchemytracker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "High Alchemy Tracker",
	description = "Tracks successful High Level Alchemy casts and the gross GP they create.",
	tags = {"alch", "alchemy", "gp", "magic", "tracker"}
)
public class HighAlchemyTrackerPlugin extends Plugin
{
	static final String CONFIG_GROUP = "high-alchemy-tracker";
	static final String ITEM_KEY_PREFIX = "item_";

	private static final String ALCH_TARGET = "High Level Alchemy";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientToolbar clientToolbar;

	private final Map<Integer, AlchemyRecord> records = new HashMap<>();
	private final AlchemyCastTracker castTracker = new AlchemyCastTracker();

	private long sessionCasts;
	private long sessionGrossGp;
	private HighAlchemyTrackerPanel panel;
	private NavigationButton navigationButton;

	@Override
	protected void startUp()
	{
		panel = new HighAlchemyTrackerPanel(() -> clientThread.invoke(this::resetTotals));
		navigationButton = NavigationButton.builder()
			.tooltip("High Alchemy Tracker")
			.icon(createNavigationIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				castTracker.setMagicXpBaseline(client.getSkillExperience(Skill.MAGIC));
			}
			loadRecords();
		});
	}

	@Override
	protected void shutDown()
	{
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}

		records.clear();
		castTracker.reset();
		sessionCasts = 0;
		sessionGrossGp = 0;
		panel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		final String option = event.getMenuOption();
		final String target = event.getMenuTarget();
		if (!"Cast".equals(option) || target == null || !target.contains(ALCH_TARGET))
		{
			return;
		}

		int itemId = event.getItemId();
		if (itemId < 0)
		{
			final Widget parent = client.getWidget(event.getParam1());
			final int slot = event.getParam0();
			if (parent != null && slot >= 0)
			{
				final Widget child = parent.getChild(slot);
				if (child != null)
				{
					itemId = child.getItemId();
				}
			}
		}

		if (itemId < 0)
		{
			return;
		}

		final int canonicalItemId = itemManager.canonicalize(itemId);
		final ItemComposition item = itemManager.getItemComposition(canonicalItemId);
		if (item == null)
		{
			return;
		}

		castTracker.prepareCast(canonicalItemId, item.getHaPrice(), client.getTickCount());
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.MAGIC)
		{
			return;
		}

		final AlchemyCastTracker.CompletedCast completedCast =
			castTracker.onMagicXpChanged(event.getXp(), client.getTickCount());
		if (completedCast != null)
		{
			recordCast(completedCast.getItemId(), completedCast.getGrossGp());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		castTracker.expirePendingCast(client.getTickCount());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			castTracker.setMagicXpBaseline(client.getSkillExperience(Skill.MAGIC));
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			castTracker.reset();
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		sessionCasts = 0;
		sessionGrossGp = 0;
		castTracker.reset();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			castTracker.setMagicXpBaseline(client.getSkillExperience(Skill.MAGIC));
		}
		loadRecords();
	}

	private void recordCast(int itemId, int grossGp)
	{
		final AlchemyRecord record = records.computeIfAbsent(itemId, ignored -> new AlchemyRecord());
		record.addCast(grossGp);
		sessionCasts++;
		sessionGrossGp += grossGp;

		configManager.setRSProfileConfiguration(
			CONFIG_GROUP,
			ITEM_KEY_PREFIX + itemId,
			AlchemyRecordCodec.encode(record));
		refreshPanel();
	}

	private void loadRecords()
	{
		records.clear();
		final String profile = configManager.getRSProfileKey();
		if (profile != null)
		{
			for (String key : configManager.getRSProfileConfigurationKeys(CONFIG_GROUP, profile, ITEM_KEY_PREFIX))
			{
				final int itemId;
				try
				{
					itemId = Integer.parseInt(key.substring(ITEM_KEY_PREFIX.length()));
				}
				catch (NumberFormatException ex)
				{
					log.debug("Ignoring invalid High Alchemy Tracker key: {}", key);
					continue;
				}

				final String value = configManager.getRSProfileConfiguration(CONFIG_GROUP, key);
				final AlchemyRecord record = AlchemyRecordCodec.decode(value);
				if (record != null)
				{
					records.put(itemId, record);
				}
			}
		}
		refreshPanel();
	}

	private void resetTotals()
	{
		final String profile = configManager.getRSProfileKey();
		if (profile != null)
		{
			for (String key : configManager.getRSProfileConfigurationKeys(CONFIG_GROUP, profile, ITEM_KEY_PREFIX))
			{
				configManager.unsetRSProfileConfiguration(CONFIG_GROUP, key);
			}
		}

		records.clear();
		sessionCasts = 0;
		sessionGrossGp = 0;
		refreshPanel();
	}

	private void refreshPanel()
	{
		if (panel == null)
		{
			return;
		}

		long lifetimeCasts = 0;
		long lifetimeGrossGp = 0;
		final List<TrackerSnapshot.ItemRow> items = new ArrayList<>();
		for (Map.Entry<Integer, AlchemyRecord> entry : records.entrySet())
		{
			final AlchemyRecord record = entry.getValue();
			lifetimeCasts += record.getCasts();
			lifetimeGrossGp += record.getGrossGp();

			final ItemComposition item = itemManager.getItemComposition(entry.getKey());
			final String name = item == null ? "Item " + entry.getKey() : item.getName();
			items.add(new TrackerSnapshot.ItemRow(name, record.getCasts(), record.getGrossGp()));
		}

		items.sort(Comparator.comparingLong(TrackerSnapshot.ItemRow::getGrossGp).reversed()
			.thenComparing(TrackerSnapshot.ItemRow::getName));
		final TrackerSnapshot snapshot = new TrackerSnapshot(
			lifetimeCasts,
			lifetimeGrossGp,
			sessionCasts,
			sessionGrossGp,
			items);
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.update(snapshot);
			}
		});
	}

	private static BufferedImage createNavigationIcon()
	{
		final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(244, 196, 48));
		graphics.fillOval(2, 3, 11, 11);
		graphics.setColor(new Color(157, 105, 20));
		graphics.setStroke(new BasicStroke(1.2f));
		graphics.drawOval(2, 3, 11, 11);
		graphics.setColor(new Color(255, 244, 166));
		graphics.drawLine(7, 5, 7, 11);
		graphics.drawLine(5, 8, 9, 8);
		graphics.setColor(Color.WHITE);
		graphics.drawLine(12, 1, 12, 5);
		graphics.drawLine(10, 3, 14, 3);
		graphics.dispose();
		return image;
	}
}
