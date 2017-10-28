/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.communityboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.la2eden.Config;
import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.cache.HtmCache;
import com.la2eden.gameserver.data.sql.impl.ClanTable;
import com.la2eden.gameserver.data.xml.impl.BuyListData;
import com.la2eden.gameserver.data.xml.impl.MultisellData;
import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.handler.CommunityBoardHandler;
import com.la2eden.gameserver.handler.IParseBoardHandler;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.zone.ZoneId;
import com.la2eden.gameserver.network.serverpackets.BuyList;
import com.la2eden.gameserver.network.serverpackets.ExBuySellList;
import com.la2eden.gameserver.network.serverpackets.ShowBoard;

/**
 * Home board.
 * @author Zoey76, Mobius
 */
public final class HomeBoard implements IParseBoardHandler
{
	// SQL Queries
	private static final String COUNT_FAVORITES = "SELECT COUNT(*) AS favorites FROM `bbs_favorites` WHERE `playerId`=?";
	
	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
		"_bbsmultisell",
		"_bbssell",
		"_bbsteleport",
		"_bbsbuff"
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMANDS;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, L2PcInstance activeChar)
	{
		if (Config.CUSTOM_CB_ENABLED)
		{
			if (Config.COMMUNITYBOARD_COMBAT_DISABLED && (activeChar.isInCombat() || activeChar.isInDuel() || activeChar.isInOlympiadMode() || activeChar.isInsideZone(ZoneId.SIEGE) || activeChar.isInsideZone(ZoneId.PVP)))
			{
				activeChar.sendMessage("You can't use the Community Board right now.");
				return false;
			}
			if (Config.COMMUNITYBOARD_KARMA_DISABLED && (activeChar.getKarma() > 0))
			{
				activeChar.sendMessage("Players with Karma cannot use the Community Board.");
				return false;
			}
		}
		
		if (command.equals("_bbshome") || command.equals("_bbstop"))
		{
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			CommunityBoardHandler.getInstance().addBypass(activeChar, "Home", command);
			
			String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/CommunityBoard/" + customPath + "home.html");
			html = html.replaceAll("%fav_count%", Integer.toString(getFavoriteCount(activeChar)));
			html = html.replaceAll("%region_count%", Integer.toString(getRegionCount(activeChar)));
			html = html.replaceAll("%clan_count%", Integer.toString(ClanTable.getInstance().getClanCount()));
			CommunityBoardHandler.separateAndSend(html, activeChar);
		}
		else if (command.startsWith("_bbstop;"))
		{
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			final String path = command.replace("_bbstop;", "");
			if ((path.length() > 0) && path.endsWith(".html"))
			{
				final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/CommunityBoard/" + customPath + path);
				CommunityBoardHandler.separateAndSend(html, activeChar);
			}
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_MULTISELLS && command.startsWith("_bbsmultisell"))
		{
			final String fullBypass = command.replace("_bbsmultisell;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int multisellId = Integer.parseInt(buypassOptions[0]);
			final String page = buypassOptions[1];
			final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/CommunityBoard/Custom/" + page + ".html");
			CommunityBoardHandler.separateAndSend(html, activeChar);
			MultisellData.getInstance().separateAndSend(multisellId, activeChar, null, false);
			return true;
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_MULTISELLS && command.startsWith("_bbssell"))
		{
			final String page = command.replace("_bbssell;", "");
			final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/CommunityBoard/Custom/" + page + ".html");
			CommunityBoardHandler.separateAndSend(html, activeChar);
			activeChar.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), activeChar.getAdena(), 0));
			activeChar.sendPacket(new ExBuySellList(activeChar, false));
			return true;
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_TELEPORTS && command.startsWith("_bbsteleport"))
		{
			final String fullBypass = command.replace("_bbsteleport;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int x = Integer.parseInt(buypassOptions[0]);
			final int y = Integer.parseInt(buypassOptions[1]);
			final int z = Integer.parseInt(buypassOptions[2]);
			if (activeChar.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_TELEPORT_PRICE)
			{
				activeChar.sendMessage("Not enough currency!");
				return false;
			}
			activeChar.sendPacket(new ShowBoard());
			activeChar.getInventory().destroyItemByItemId("CB_Teleport", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_TELEPORT_PRICE, activeChar, activeChar);
			activeChar.teleToLocation(x, y, z, 0);
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_BUFFS && command.startsWith("_bbsbuff"))
		{
			final String fullBypass = command.replace("_bbsbuff;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int buffId = Integer.parseInt(buypassOptions[0]);
			final int buffLevel = Integer.parseInt(buypassOptions[1]);
			final String page = buypassOptions[2];
			if (activeChar.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_BUFF_PRICE)
			{
				activeChar.sendMessage("Not enough currency!");
			}
			else
			{
				activeChar.getInventory().destroyItemByItemId("CB_Buff", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_BUFF_PRICE, activeChar, activeChar);
				SkillData.getInstance().getSkill(buffId, buffLevel).applyEffects(activeChar, activeChar);
			}
			final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "./datapack/html/CommunityBoard/Custom/" + page + ".html");
			CommunityBoardHandler.separateAndSend(html, activeChar);
		}
		return false;
	}
	
	/**
	 * Gets the Favorite links for the given player.
	 * @param player the player
	 * @return the favorite links count
	 */
	private static int getFavoriteCount(L2PcInstance player)
	{
		int count = 0;
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(COUNT_FAVORITES))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					count = rs.getInt("favorites");
				}
			}
		}
		catch (Exception e)
		{
			LOG.warning(FavoriteBoard.class.getSimpleName() + ": Coudn't load favorites count for player " + player.getName());
		}
		return count;
	}
	
	/**
	 * Gets the registered regions count for the given player.
	 * @param player the player
	 * @return the registered regions count
	 */
	private static int getRegionCount(L2PcInstance player)
	{
		return 0; // TODO: Implement.
	}
}
