package com.partypoints;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class PartyPointsOverlay extends OverlayPanel
{
	private final PartyPointsPlugin plugin;
	@Inject
	private final PartyPointsConfig config;
	public Map<String, Integer> PartyData = new HashMap<>();
	PartyPointsOverlay(PartyPointsPlugin plugin, PartyPointsConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPriority(OverlayPriority.MED);
		addMenuEntry(RUNELITE_OVERLAY_CONFIG,OPTION_CONFIGURE,"PartyPoints Overlay");
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
		return super.render(graphics);

	}
}
