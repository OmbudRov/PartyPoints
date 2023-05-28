package com.partypoints;

import com.google.inject.Inject;
import com.partypoints.data.PartyData;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.DragAndDropReorderPane;
import net.runelite.client.ui.components.PluginErrorPanel;

public class PartyPointsPanel extends PluginPanel
{
	private static final String BTN_CREATE_TEXT = "Create Party";
	private static final String BTN_LEAVE_TEXT = "Leave";

	private final PartyPointsPlugin plugin;
	private final PartyService party;
	private final PartyPointsConfig config;

	private final Map<Long, PartyPointsMembers> membersMap = new HashMap<>();

	private final JButton startButton = new JButton();
	private final JButton joinPartyButton = new JButton();
	private final JButton rejoinPartyButton = new JButton();
	private final JButton copyPartyIdButton = new JButton();
	private final JButton refreshPartyButton = new JButton();

	private final PluginErrorPanel noPartyPanel = new PluginErrorPanel();
	private final PluginErrorPanel emptyPartyPanel = new PluginErrorPanel();
	private final JComponent memberBoxPanel = new DragAndDropReorderPane();

	@Inject
	PartyPointsPanel(final ClientThread clientThread, final PartyPointsPlugin plugin, final PartyPointsConfig config, final PartyService party)
	{
		this.plugin = plugin;
		this.party = party;
		this.config = config;

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		final JPanel layoutPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
		layoutPanel.setLayout(boxLayout);
		add(layoutPanel, BorderLayout.NORTH);

		final JPanel topPanel = new JPanel();

		topPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
		topPanel.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 2, 4, 2);

		constraints.gridx = 0;
		constraints.gridy = 0;
		topPanel.add(startButton, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		topPanel.add(joinPartyButton, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		topPanel.add(copyPartyIdButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		topPanel.add(rejoinPartyButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		topPanel.add(refreshPartyButton, constraints);

		layoutPanel.add(topPanel);
		layoutPanel.add(memberBoxPanel);

		startButton.setText(party.isInParty() ? BTN_LEAVE_TEXT : BTN_CREATE_TEXT);
		startButton.setFocusable(false);

		joinPartyButton.setText("Join party");
		joinPartyButton.setFocusable(false);

		rejoinPartyButton.setText("Join previous party");
		rejoinPartyButton.setFocusable(false);

		copyPartyIdButton.setText("Copy passphrase");
		copyPartyIdButton.setFocusable(false);

		refreshPartyButton.setText("Refresh the Overlay");
		refreshPartyButton.setFocusable(false);

		startButton.addActionListener(e ->
		{
			if (party.isInParty())
			{
				// Leave party
				final int result = JOptionPane.showOptionDialog(startButton,
					"Are you sure you want to leave the party?",
					"Leave party?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, new String[]{"Yes", "No"}, "No");

				if (result == JOptionPane.YES_OPTION)
				{
					plugin.leaveParty();
				}
			}
			else
			{
				// Create party
				clientThread.invokeLater(() -> party.changeParty(party.generatePassphrase()));
			}
		});

		joinPartyButton.addActionListener(e ->
		{
			if (!party.isInParty())
			{
				String s = (String) JOptionPane.showInputDialog(
					joinPartyButton,
					"Please enter the party passphrase:",
					"Party Passphrase",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					"");

				if (s == null)
				{
					return;
				}

				for (int i = 0; i < s.length(); ++i)
				{
					char ch = s.charAt(i);
					if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '-')
					{
						JOptionPane.showMessageDialog(joinPartyButton,
							"Party passphrase must be a combination of alphanumeric or hyphen characters.",
							"Invalid party passphrase",
							JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				party.changeParty(s);
			}
		});

		rejoinPartyButton.addActionListener(e ->
		{
			if (!party.isInParty())
			{
				party.changeParty(config.previousPartyId());
			}
		});

		copyPartyIdButton.addActionListener(e ->
		{
			if (party.isInParty())
			{
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(party.getPartyPassphrase()), null);
			}
		});

		refreshPartyButton.addActionListener(e ->
		{
			if (party.isInParty())
			{
				plugin.leaveParty();
				try
				{
					Thread.sleep(600);
				}
				catch (InterruptedException ignored)
				{
				}
				party.changeParty(config.previousPartyId());
			}
		});

		noPartyPanel.setContent("Not in a party", "Create a party to begin");

		updateParty();
	}

	void updateParty()
	{
		remove(noPartyPanel);
		remove(emptyPartyPanel);

		startButton.setText(party.isInParty() ? BTN_LEAVE_TEXT : BTN_CREATE_TEXT);
		joinPartyButton.setVisible(!party.isInParty());
		rejoinPartyButton.setVisible(!party.isInParty());
		copyPartyIdButton.setVisible(party.isInParty());
		refreshPartyButton.setVisible(party.isInParty());

		if (!party.isInParty())
		{
			add(noPartyPanel);
		}
		else if (plugin.getPartyDataMap().size() <= 1)
		{
			emptyPartyPanel.setContent("Party Created", "Your Party Passphrase is: " + party.getPartyPassphrase() + ".");
			add(emptyPartyPanel);
		}
	}

	void addMember(PartyData partyData)
	{
		if (!membersMap.containsKey(partyData.getMemberId()))
		{
			PartyPointsMembers partyMember = new PartyPointsMembers(plugin, config, memberBoxPanel, partyData, party);
			membersMap.put(partyData.getMemberId(), partyMember);
			memberBoxPanel.add(partyMember);
			memberBoxPanel.revalidate();
		}
		updateParty();
	}

	void removeAllMembers()
	{
		membersMap.forEach((key, value) -> memberBoxPanel.remove(value));
		memberBoxPanel.revalidate();
		membersMap.clear();
		updateParty();
	}

	void removeMember(long memberId)
	{
		final PartyPointsMembers members = membersMap.remove(memberId);

		if (members != null)
		{
			memberBoxPanel.remove(members);
			memberBoxPanel.revalidate();
		}
		updateParty();
	}

	void updateMember(long userId)
	{
		final PartyPointsMembers members = membersMap.get(userId);
		if (members != null)
		{
			members.update(this.plugin);
		}
	}

	void updateAll()
	{
		membersMap.forEach((key, value) -> value.update(this.plugin));
	}
}
