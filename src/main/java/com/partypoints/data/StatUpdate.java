package com.partypoints.data;

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
public class StatUpdate extends PartyMemberMessage
{
	@SerializedName("n")
	private String cName;

	@SerializedName("pp")
	private Integer CurrentPersonalPoints;

}
