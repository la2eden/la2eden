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
package handlers.admincommandhandlers;

import java.util.Collection;
import java.util.StringTokenizer;

import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.CursedWeaponsManager;
import com.la2eden.gameserver.model.CursedWeapon;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - cw_info = displays cursed weapon status - cw_remove = removes a cursed weapon from the world, item id or name must be provided - cw_add = adds a cursed weapon into the world, item id or name must be provided. Target will be the weilder - cw_goto =
 * teleports GM to the specified cursed weapon - cw_reload = reloads instance manager
 * @version $Revision: 1.1.6.3 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cw_info",
		"admin_cw_remove",
		"admin_cw_goto",
		"admin_cw_reload",
		"admin_cw_add",
		"admin_cw_info_menu"
	};
	
	private int itemId;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		
		final CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
		int id = 0;
		
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_cw_info"))
		{
			if (!command.contains("menu"))
			{
				activeChar.sendMessage("====== Cursed Weapons: ======");
				for (CursedWeapon cw : cwm.getCursedWeapons())
				{
					activeChar.sendMessage("> " + cw.getName() + " (" + cw.getItemId() + ")");
					if (cw.isActivated())
					{
						final L2PcInstance pl = cw.getPlayer();
						activeChar.sendMessage("  Player holding: " + (pl == null ? "null" : pl.getName()));
						activeChar.sendMessage("    Player karma: " + cw.getPlayerKarma());
						activeChar.sendMessage("    Time Remaining: " + (cw.getTimeLeft() / 60000) + " min.");
						activeChar.sendMessage("    Kills : " + cw.getNbKills());
					}
					else if (cw.isDropped())
					{
						activeChar.sendMessage("  Lying on the ground.");
						activeChar.sendMessage("    Time Remaining: " + (cw.getTimeLeft() / 60000) + " min.");
						activeChar.sendMessage("    Kills : " + cw.getNbKills());
					}
					else
					{
						activeChar.sendMessage("  Don't exist in the world.");
					}
					activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
				}
			}
			else
			{
				final Collection<CursedWeapon> cws = cwm.getCursedWeapons();
				final StringBuilder replyMSG = new StringBuilder(cws.size() * 300);
				final NpcHtmlMessage adminReply = new NpcHtmlMessage();
				adminReply.setFile(activeChar.getHtmlPrefix(), "./datapack/html/admin/cwinfo.htm");
				for (CursedWeapon cw : cwm.getCursedWeapons())
				{
					itemId = cw.getItemId();
					
					replyMSG.append("<table width=270><tr><td>Name:</td><td>");
					replyMSG.append(cw.getName());
					replyMSG.append("</td></tr>");
					
					if (cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();
						replyMSG.append("<tr><td>Weilder:</td><td>");
						replyMSG.append(pl == null ? "null" : pl.getName());
						replyMSG.append("</td></tr>");
						replyMSG.append("<tr><td>Karma:</td><td>");
						replyMSG.append(cw.getPlayerKarma());
						replyMSG.append("</td></tr>");
						replyMSG.append("<tr><td>Kills:</td><td>");
						replyMSG.append(cw.getPlayerPkKills());
						replyMSG.append("/");
						replyMSG.append(cw.getNbKills());
						replyMSG.append("</td></tr><tr><td>Time remaining:</td><td>");
						replyMSG.append(cw.getTimeLeft() / 60000);
						replyMSG.append(" min.</td></tr>");
						replyMSG.append("<tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ");
						replyMSG.append(itemId);
						replyMSG.append("\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
						replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto ");
						replyMSG.append(itemId);
						replyMSG.append("\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
					}
					else if (cw.isDropped())
					{
						replyMSG.append("<tr><td>Position:</td><td>Lying on the ground</td></tr><tr><td>Time remaining:</td><td>");
						replyMSG.append(cw.getTimeLeft() / 60000);
						replyMSG.append(" min.</td></tr><tr><td>Kills:</td><td>");
						replyMSG.append(cw.getNbKills());
						replyMSG.append("</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ");
						replyMSG.append(itemId);
						replyMSG.append("\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
						replyMSG.append("<td><button value=\"Go\" action=\"bypass -h admin_cw_goto ");
						replyMSG.append(itemId);
						replyMSG.append("\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
					}
					else
					{
						replyMSG.append("<tr><td>Position:</td><td>Doesn't exist.</td></tr><tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add ");
						replyMSG.append(itemId);
						replyMSG.append("\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td></td></tr>");
					}
					
					replyMSG.append("</table><br>");
				}
				adminReply.replace("%cwinfo%", replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
		}
		else if (command.startsWith("admin_cw_reload"))
		{
			cwm.reload();
		}
		else
		{
			CursedWeapon cw = null;
			try
			{
				String parameter = st.nextToken();
				if (parameter.matches("[0-9]*"))
				{
					id = Integer.parseInt(parameter);
				}
				else
				{
					parameter = parameter.replace('_', ' ');
					for (CursedWeapon cwp : cwm.getCursedWeapons())
					{
						if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
						{
							id = cwp.getItemId();
							break;
						}
					}
				}
				cw = cwm.getCursedWeapon(id);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>");
			}
			
			if (cw == null)
			{
				activeChar.sendMessage("Unknown cursed weapon ID.");
				return false;
			}
			
			if (command.startsWith("admin_cw_remove "))
			{
				cw.endOfLife();
			}
			else if (command.startsWith("admin_cw_goto "))
			{
				cw.goTo(activeChar);
			}
			else if (command.startsWith("admin_cw_add"))
			{
				if (cw.isActive())
				{
					activeChar.sendMessage("This cursed weapon is already active.");
				}
				else
				{
					final L2Object target = activeChar.getTarget();
					if (target instanceof L2PcInstance)
					{
						((L2PcInstance) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					}
					else
					{
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
					}
					cw.setEndTime(System.currentTimeMillis() + (cw.getDuration() * 60000L));
					cw.reActivate();
				}
			}
			else
			{
				activeChar.sendMessage("Unknown command.");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}