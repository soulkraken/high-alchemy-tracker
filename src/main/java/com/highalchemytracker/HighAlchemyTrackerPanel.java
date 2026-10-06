package com.highalchemytracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

final class HighAlchemyTrackerPanel extends PluginPanel
{
	private final Runnable resetAction;
	private final JLabel lifetimeCasts = valueLabel();
	private final JLabel lifetimeGp = valueLabel();
	private final JLabel sessionCasts = valueLabel();
	private final JLabel sessionGp = valueLabel();
	private final JPanel itemList = new JPanel();

	HighAlchemyTrackerPanel(Runnable resetAction)
	{
		this.resetAction = resetAction;

		final JLabel title = new JLabel("High Alchemy Tracker");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
		add(title);

		add(section("Lifetime", lifetimeCasts, lifetimeGp));
		add(section("This session", sessionCasts, sessionGp));

		final JLabel itemsTitle = new JLabel("Items");
		itemsTitle.setFont(FontManager.getRunescapeBoldFont());
		itemsTitle.setForeground(Color.WHITE);
		itemsTitle.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));
		add(itemsTitle);

		itemList.setLayout(new BoxLayout(itemList, BoxLayout.Y_AXIS));
		itemList.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(itemList);

		final JButton resetButton = new JButton("Reset all totals");
		resetButton.setFocusable(false);
		resetButton.addActionListener(event -> confirmReset());
		add(resetButton);
	}

	void update(TrackerSnapshot snapshot)
	{
		lifetimeCasts.setText(format(snapshot.getLifetimeCasts()));
		lifetimeGp.setText(format(snapshot.getLifetimeGrossGp()) + " gp");
		sessionCasts.setText(format(snapshot.getSessionCasts()));
		sessionGp.setText(format(snapshot.getSessionGrossGp()) + " gp");

		itemList.removeAll();
		if (snapshot.getItems().isEmpty())
		{
			final JLabel empty = new JLabel("No completed casts yet.");
			empty.setForeground(Color.GRAY);
			empty.setAlignmentX(Component.LEFT_ALIGNMENT);
			empty.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
			itemList.add(empty);
		}
		else
		{
			for (TrackerSnapshot.ItemRow item : snapshot.getItems())
			{
				itemList.add(itemRow(item));
				itemList.add(Box.createRigidArea(new Dimension(0, 3)));
			}
		}

		itemList.revalidate();
		itemList.repaint();
	}

	private JPanel section(String heading, JLabel casts, JLabel gp)
	{
		final JPanel panel = new JPanel(new BorderLayout(0, 4));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 7, 6, 7));

		final JLabel headingLabel = new JLabel(heading);
		headingLabel.setFont(FontManager.getRunescapeBoldFont());
		headingLabel.setForeground(Color.WHITE);
		panel.add(headingLabel, BorderLayout.NORTH);

		final JPanel values = new JPanel(new GridLayout(2, 2, 4, 2));
		values.setOpaque(false);
		values.add(caption("Casts"));
		values.add(casts);
		values.add(caption("Gross GP"));
		values.add(gp);
		panel.add(values, BorderLayout.CENTER);
		return panel;
	}

	private JPanel itemRow(TrackerSnapshot.ItemRow item)
	{
		final JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(6, 7, 6, 7));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		final JLabel name = new JLabel(item.getName());
		name.setForeground(Color.WHITE);
		row.add(name, BorderLayout.NORTH);

		final JLabel details = new JLabel(format(item.getCasts()) + " casts  •  "
			+ format(item.getGrossGp()) + " gp");
		details.setForeground(Color.LIGHT_GRAY);
		row.add(details, BorderLayout.CENTER);
		return row;
	}

	private void confirmReset()
	{
		final int choice = JOptionPane.showConfirmDialog(
			this,
			"Delete all lifetime and session High Alchemy totals for this character?",
			"Reset High Alchemy Tracker",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		if (choice == JOptionPane.YES_OPTION)
		{
			resetAction.run();
		}
	}

	private static JLabel caption(String text)
	{
		final JLabel label = new JLabel(text);
		label.setForeground(Color.LIGHT_GRAY);
		return label;
	}

	private static JLabel valueLabel()
	{
		final JLabel label = new JLabel("0", SwingConstants.RIGHT);
		label.setForeground(Color.WHITE);
		return label;
	}

	private static String format(long value)
	{
		return String.format(Locale.US, "%,d", value);
	}
}
