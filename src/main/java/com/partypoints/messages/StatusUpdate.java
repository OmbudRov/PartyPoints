package com.partypoints.messages;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.runelite.client.party.messages.PartyMemberMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StatusUpdate extends PartyMemberMessage
{
	@SerializedName("n")
	private String characterName = null;

	@SerializedName("pp")
	private Integer CurrentPersonalPoints = null;
}
