/*
 * This file is part of the La2Eden project.
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

import java.util.StringTokenizer;

import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.RaidBossSpawnManager;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.L2Spawn;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author NosBit
 */
public class AdminScan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_scan",
		"admin_deleteNpcByObjectId"
	};
	
	private static final int DEFAULT_RADIUS = 500;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		switch (actualCommand.toLowerCase())
		{
			case "admin_scan":
			{
				int radius = DEFAULT_RADIUS;
				if (st.hasMoreElements())
				{
					try
					{
						radius = Integer.parseInt(st.nextToken());
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //scan [radius]");
						return false;
					}
				}
				
				sendNpcList(activeChar, radius);
				break;
			}
			case "admin_deletenpcbyobjectid":
			{
				if (!st.hasMoreElements())
				{
					activeChar.sendMessage("Usage: //deletenpcbyobjectid <object_id>");
					return false;
				}
				
				try
				{
					final int objectId = Integer.parseInt(st.nextToken());
					
					final L2Object target = L2World.getInstance().findObject(objectId);
					final L2Npc npc = target instanceof L2Npc ? (L2Npc) target : null;
					if (npc == null)
					{
						activeChar.sendMessage("NPC does not exist or object_id does not belong to an NPC");
						return false;
					}
					
					npc.deleteMe();
					
					final L2Spawn spawn = npc.getSpawn();
					if (spawn != null)
					{
						spawn.stopRespawn();
						
						if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId()))
						{
							RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
						}
						else
						{
							SpawnTable.getInstance().deleteSpawn(spawn, true);
						}
					}
					
					activeChar.sendMessage(npc.getName() + " have been deleted.");
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage("object_id must be a number.");
					return false;
				}
				
				sendNpcList(activeChar, DEFAULT_RADIUS);
				break;
			}
		}
		return true;
	}
	
	private void sendNpcList(L2PcInstance activeChar, int radius)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "./datapack/html/admin/scan.htm");
		final StringBuilder sb = new StringBuilder();
		for (L2Character character : activeChar.getKnownList().getKnownCharactersInRadius(radius))
		{
			if (character instanceof L2Npc)
			{
				sb.append("<tr>");
				sb.append("<td width=\"54\">" + character.getId() + "</td>");
				sb.append("<td width=\"54\">" + character.getName() + "</td>");
				sb.append("<td width=\"54\">" + Math.round(activeChar.calculateDistance(character, false, false)) + "</td>");
				sb.append("<td width=\"54\"><a action=\"bypass -h admin_deleteNpcByObjectId " + character.getObjectId() + "\"><font color=\"LEVEL\">Delete</font></a></td>");
				sb.append("<td width=\"54\"><a action=\"bypass -h admin_move_to " + character.getX() + " " + character.getY() + " " + character.getZ() + "\"><font color=\"LEVEL\">Go to</font></a></td>");
				sb.append("</tr>");
			}
		}
		html.replace("%data%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
