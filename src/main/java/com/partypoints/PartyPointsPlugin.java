package com.partypoints;

import com.google.common.base.Strings;
import com.google.inject.Provides;
import com.partypoints.data.PartyData;
import com.partypoints.data.StatUpdate;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.events.PartyMemberAvatar;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserJoin;
import net.runelite.client.party.events.UserPart;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "PartyPoints"
)
public class PartyPointsPlugin extends Plugin
{
	@Getter
	private final Map<Long, PartyData> partyDataMap = Collections.synchronizedMap(new HashMap<>());
	@Inject
	private Client client;
	@Inject
	private PartyService party;
	@Inject
	private WSClient wsClient;
	@Inject
	private PartyPointsConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private OverlayManager overlayManager;
	private PartyPointsPanel panel;
	private NavigationButton navButton;

	private StatUpdate lastStatus;

	private PartyPointsOverlay partyPointsOverlay = null;
	private boolean button = false;

	private static int messageFreq(int partySize)
	{
		return Math.max(1, partySize - 6);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(PartyPointsPanel.class);

		// Load icon for the Panel
		final BufferedImage icon = ImageUtil.loadImageResource(PartyPointsPlugin.class, "/peepoPurple.png");

		// Build Panel
		navButton = NavigationButton.builder()
			.tooltip("PartyPoints")
			.priority(9)
			.icon(icon)
			.panel(panel)
			.build();

		// Add Panel to Sidebar
		if (config.AlwaysShowIcon())
		{
			button = true;
			clientToolbar.addNavigation(navButton);
		}

		wsClient.registerMessage(StatUpdate.class);
		SwingUtilities.invokeLater(this::requestSync);

		partyPointsOverlay = new PartyPointsOverlay(this, config);
	}

	@Override
	protected void shutDown() throws Exception
	{
		updateOverlay(client.getLocalPlayer().getName(),-1);
		// Remove Panel from Sidebar
		clientToolbar.removeNavigation(navButton);

		wsClient.unregisterMessage(StatUpdate.class);

		// Clear out all data
		partyDataMap.clear();
		panel = null;
		lastStatus = null;
		button = false;
		overlayManager.remove(partyPointsOverlay);
	}

	@Provides
	public PartyPointsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyPointsConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(PartyPointsConfig.GROUP))
		{
			SwingUtilities.invokeLater(panel::updateAll);
		}
		if (config.AlwaysShowIcon())
		{
			if (!button)
			{
				clientToolbar.addNavigation(navButton);
				button = false;
			}
		}
		else if (button && !party.isInParty())
		{
			clientToolbar.removeNavigation(navButton);
			button = true;
		}
		button = config.AlwaysShowIcon();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		checkStateChanged(false);
	}

	@Subscribe
	public void onGameTick(final GameTick tick)
	{
		checkStateChanged(false);
		overlayManager.remove(partyPointsOverlay);
		if (party.isInParty() && config.ShowOverlay())
		{
			overlayManager.add(partyPointsOverlay);
		}
	}

	@Subscribe
	public void onStatUpdate(final StatUpdate event)
	{
		final PartyData partyData = getPartyData(event.getMemberId());
		if (partyData == null)
		{
			return;
		}

		if (event.getCurrentPersonalPoints() != null)
		{
			partyData.setPersonalPoints(event.getCurrentPersonalPoints());
		}

		final PartyMember member = party.getMemberById(event.getMemberId());
		if (event.getCName() != null)
		{
			final String name = Text.removeTags(Text.toJagexName(event.getCName()));
			if (!name.isEmpty())
			{
				member.setDisplayName(name);
				member.setLoggedIn(true);
				partyData.setColor(ColorUtil.fromObject(name));
			}
			else
			{
				member.setLoggedIn(false);
				partyData.setColor(Color.WHITE);
			}
		}
		SwingUtilities.invokeLater(() -> panel.updateMember(event.getMemberId()));
	}

	@Subscribe
	public void onUserJoin(final UserJoin event)
	{
		getPartyData(event.getMemberId());
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		if (!button)
		{
			clientToolbar.addNavigation(navButton);
			button = true;
		}
		clientThread.invokeLater(() -> checkStateChanged(true));
	}

	@Subscribe
	public void onUserPart(final UserPart event)
	{
		final PartyData removed = partyDataMap.remove(event.getMemberId());
		if (removed != null)
		{
			SwingUtilities.invokeLater(() -> panel.removeMember(event.getMemberId()));
		}
		if (button && (!party.isInParty() || party.getMembers().size() == 0) && !config.AlwaysShowIcon())
		{
			clientToolbar.removeNavigation(navButton);
			button = false;
		}
	}

	@Subscribe
	public void onPartyChanged(final PartyChanged event)
	{
		// Reset party
		partyDataMap.clear();
		if (event.getPartyId() != null)
		{
			config.setPreviousPartyId(event.getPassphrase());
		}
		SwingUtilities.invokeLater(panel::removeAllMembers);
	}

	@Subscribe
	public void onPartyMemberAvatar(PartyMemberAvatar event)
	{
		SwingUtilities.invokeLater(() -> panel.updateMember(event.getMemberId()));
	}

	@Nullable
	PartyData getPartyData(final long uuid)
	{
		final PartyMember memberById = party.getMemberById(uuid);

		if (memberById == null)
		{
			return null;
		}
		return partyDataMap.computeIfAbsent(uuid, (u) -> {
			PartyMember partyMember = party.getLocalMember();

			PartyData partyData = new PartyData(uuid);

			SwingUtilities.invokeLater(() -> panel.addMember(partyData));
			return partyData;
		});
	}

	void requestSync()
	{
		if (party.isInParty())
		{
			// Request sync
			final UserSync userSync = new UserSync();
			party.send(userSync);
		}
	}

	void leaveParty()
	{
		resetParty();
		party.changeParty(null);
		overlayManager.remove(partyPointsOverlay);
	}

	private void checkStateChanged(boolean forceSend)
	{
		if (lastStatus == null)
		{
			forceSend = true;
		}
		if (!party.isInParty())
		{
			return;
		}
		if (!forceSend && client.getTickCount() % messageFreq(party.getMembers().size()) != 0)
		{
			return;
		}

		final int CurrentPersonalPoints = client.getVarbitValue(Varbits.PERSONAL_POINTS);

		final Player localPlayer = client.getLocalPlayer();
		final String characterName = Strings.nullToEmpty(localPlayer != null && client.getGameState().getState() >= GameState.LOADING.getState() ? localPlayer.getName() : null);

		boolean shouldSend = false;
		final StatUpdate statUpdate = new StatUpdate();

		if (forceSend || !characterName.equals(lastStatus.getCName()))
		{
			shouldSend = true;
			statUpdate.setCName(characterName);
		}

		if (forceSend || CurrentPersonalPoints != lastStatus.getCurrentPersonalPoints())
		{
			shouldSend = true;
			statUpdate.setCurrentPersonalPoints(CurrentPersonalPoints);
		}

		if (shouldSend)
		{
			party.send(statUpdate);
			lastStatus = new StatUpdate(characterName, CurrentPersonalPoints);
		}

	}

	protected void updateOverlay(String Name, int points)
	{
		partyPointsOverlay.PartyData.put(Name, points);
	}

	protected void resetParty()
	{
		partyPointsOverlay.PartyData.clear();
	}
}
