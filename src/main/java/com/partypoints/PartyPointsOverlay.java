package com.partypoints;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class PartyPointsOverlay extends OverlayPanel
{
	private final PartyPointsPlugin plugin;
	@Inject
	private final PartyPointsConfig config;
	private final PanelComponent panelComponent = new PanelComponent();
	public Map<String, Integer> PartyData = new HashMap<>();

	PartyPointsOverlay(PartyPointsPlugin plugin, PartyPointsConfig config)
	{
		super(plugin);
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		PartyData.remove("<unknown>");
		if (!PartyData.isEmpty())
		{
			for (Map.Entry<String, Integer> Entry : PartyData.entrySet())
			{
				if (config.overlayThreshold()< Entry.getValue())
				{
					panelComponent.getChildren().add(LineComponent.builder().left(Entry.getKey() + ": ").right(String.valueOf(Entry.getValue())).build());
				}
			}
		}
		return panelComponent.render(graphics);
	}
}
