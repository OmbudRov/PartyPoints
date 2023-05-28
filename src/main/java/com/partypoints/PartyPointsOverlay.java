package com.partypoints;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class PartyPointsOverlay extends Overlay
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
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		PartyData.remove("<unknown>");
		if (!PartyData.isEmpty())
		{
			String Title = "Party Points";
			if (config.ShowOverlayTitle())
			{
				panelComponent.getChildren().add(TitleComponent.builder().text(Title).build());
			}
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
