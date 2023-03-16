package com.partypoints;

import com.partypoints.data.PartyData;
import javax.annotation.Nullable;

public interface PartyPointsPluginService
{
	@Nullable
	PartyData getPartyData(long memberId);
}
