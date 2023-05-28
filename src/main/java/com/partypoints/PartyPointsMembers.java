package com.partypoints;

import com.partypoints.data.PartyData;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.MouseDragEventForwarder;
import net.runelite.client.util.ImageUtil;

public class PartyPointsMembers extends JPanel
{
	private final PartyData memberPartyData;
	private final PartyService partyService;

	private final JLabel name = new JLabel();
	private final JLabel avatar = new JLabel();

	private final JLabel PP = new JLabel();

	private final PartyPointsConfig config;

	private boolean avatarSet;

	private PartyPointsPlugin plugin;

	PartyPointsMembers(final PartyPointsPlugin plugin, final PartyPointsConfig config, final JComponent panel, final PartyData memberPartyData, final PartyService partyService)
	{
		this.plugin = plugin;
		this.config = config;
		this.memberPartyData = memberPartyData;
		this.partyService = partyService;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 0, 0, 0));

		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setBorder(new EmptyBorder(5, 5, 5, 5));

		Border border = BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR, 1);
		avatar.setBorder(border);

		avatar.setHorizontalAlignment(SwingConstants.CENTER);
		avatar.setVerticalAlignment(SwingConstants.CENTER);
		avatar.setPreferredSize(new Dimension(35, 35));

		final JPanel headerPanel = new JPanel();
		headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBorder(new EmptyBorder(0, 0, 3, 0));

		final JPanel namesPanel = new JPanel();
		namesPanel.setLayout(new BorderLayout());
		namesPanel.setBorder(new EmptyBorder(0, 0, 3, 0));
		namesPanel.setBorder(new EmptyBorder(2, 5, 2, 5));

		final JPanel PPPanel = new JPanel();
		PPPanel.setLayout(new BorderLayout());
		PPPanel.setBorder(new EmptyBorder(0, 0, 3, 0));
		PPPanel.setBorder(new EmptyBorder(2, 5, 2, 5));

		name.setFont(FontManager.getRunescapeFont());
		name.putClientProperty("html.disable", Boolean.TRUE);

		PP.setFont(FontManager.getRunescapeFont());
		PP.putClientProperty("html.disable", Boolean.TRUE);

		namesPanel.add(name);

		PPPanel.add(PP);

		headerPanel.add(avatar, BorderLayout.WEST);
		headerPanel.add(namesPanel, BorderLayout.CENTER);
		headerPanel.add(PPPanel, BorderLayout.EAST);

		container.add(headerPanel, BorderLayout.NORTH);
		MouseDragEventForwarder mouseDragEventForwarder = new MouseDragEventForwarder(panel);
		container.addMouseListener(mouseDragEventForwarder);
		container.addMouseMotionListener(mouseDragEventForwarder);

		add(container, BorderLayout.NORTH);

		update(plugin);
	}

	void update(final PartyPointsPlugin plugin)
	{
		PartyPointsOverlay partyPointsOverlay = new PartyPointsOverlay(this.plugin, config);
		final PartyMember member = partyService.getMemberById(memberPartyData.getMemberId());
		if (!avatarSet && member.getAvatar() != null)
		{
			ImageIcon icon = new ImageIcon(ImageUtil.resizeImage(member.getAvatar(), 32, 32));
			icon.getImage().flush();
			avatar.setIcon(icon);
			avatarSet = true;
		}

		name.setForeground(member.isLoggedIn() ? Color.white : ColorScheme.DARKER_GRAY_COLOR);
		name.setText(member.getDisplayName());
		PP.setText(String.valueOf(memberPartyData.getPersonalPoints()));
		plugin.updateOverlay(member.getDisplayName(), memberPartyData.getPersonalPoints());
	}
}
