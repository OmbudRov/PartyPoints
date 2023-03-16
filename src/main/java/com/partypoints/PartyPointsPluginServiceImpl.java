package com.partypoints;

import com.partypoints.data.PartyData;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PartyPointsPluginServiceImpl implements PartyPointsPluginService
{
	private final PartyPointsPlugin plugin;

	@Inject
	private PartyPointsPluginServiceImpl(final PartyPointsPlugin plugin)
	{
		this.plugin=plugin;
	}

	@Override
	public PartyData getPartyData(long memberId)
	{
		return plugin.getPartyData(memberId);
	}
}
