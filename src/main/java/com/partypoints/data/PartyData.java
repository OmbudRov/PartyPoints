package com.partypoints.data;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Setter
@Getter
@RequiredArgsConstructor
public class PartyData
{
	private final long memberId;
	private final PanelComponent panelComponent = new PanelComponent();
	private Color color = Color.white;

	private int PersonalPoints;
}
