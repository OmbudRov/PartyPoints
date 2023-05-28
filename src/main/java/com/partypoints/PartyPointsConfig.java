package com.partypoints;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PartyPointsConfig.GROUP)
public interface PartyPointsConfig extends Config
{
	String GROUP = "PartyPoints";

	@ConfigItem(
		keyName = "AlwaysShowIcon",
		name = "Always show sidebar",
		description = "Controls whether the sidebar icon is always shown or only shown while in a party "
	)
	default boolean AlwaysShowIcon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ShowPartyPassphrase",
		name = "Show Party Passphrase",
		description = "Controls whether the party passphrase is displayed within the UI"
	)
	default boolean ShowPartyPassphrase()
	{
		return true;
	}

	@ConfigItem(
		keyName = "OverlayTitle",
		name = "Show Overlay Title",
		description = "Controls whether the overlay title is displayed"
	)
	default boolean ShowOverlayTitle()
	{
		return true;
	}


	@ConfigItem(
		keyName = "previousPartyId",
		name = "",
		description = "",
		hidden = true
	)
	default String previousPartyId()
	{
		return "";
	}

	@ConfigItem(
		keyName = "previousPartyId",
		name = "",
		description = "",
		hidden = true
	)
	void setPreviousPartyId(String id);

	@ConfigItem(
		keyName = "overlayThreshold",
		name = "Overlay Threshold",
		description = "Set the mininmum points needed to be displayed on the overlay"
	)
	default int overlayThreshold()
	{
		return 1000;
	}
}
