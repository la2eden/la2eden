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

import java.awt.Color;

import com.la2eden.gameserver.data.xml.impl.DoorData;
import com.la2eden.gameserver.handler.IAdminCommandHandler;
import com.la2eden.gameserver.instancemanager.CastleManager;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2DoorInstance;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.entity.Castle;
import com.la2eden.gameserver.network.serverpackets.ExServerPrimitive;

/**
 * This class handles following admin commands: - open1 = open coloseum door 24190001 - open2 = open coloseum door 24190002 - open3 = open coloseum door 24190003 - open4 = open coloseum door 24190004 - openall = open all coloseum door - close1 = close coloseum door 24190001 - close2 = close coloseum
 * door 24190002 - close3 = close coloseum door 24190003 - close4 = close coloseum door 24190004 - closeall = close all coloseum door - open = open selected door - close = close selected door
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	private static DoorData _doorTable = DoorData.getInstance();
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall",
		"admin_showdoors"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		try
		{
			if (command.startsWith("admin_open "))
			{
				final int doorId = Integer.parseInt(command.substring(11));
				if (_doorTable.getDoor(doorId) != null)
				{
					_doorTable.getDoor(doorId).openMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).openMe();
						}
					}
				}
			}
			else if (command.startsWith("admin_close "))
			{
				final int doorId = Integer.parseInt(command.substring(12));
				if (_doorTable.getDoor(doorId) != null)
				{
					_doorTable.getDoor(doorId).closeMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).closeMe();
						}
					}
				}
			}
			else if (command.equals("admin_closeall"))
			{
				for (L2DoorInstance door : _doorTable.getDoors())
				{
					door.closeMe();
				}
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (L2DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			else if (command.equals("admin_openall"))
			{
				for (L2DoorInstance door : _doorTable.getDoors())
				{
					door.openMe();
				}
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (L2DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			else if (command.equals("admin_open"))
			{
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2DoorInstance)
				{
					((L2DoorInstance) target).openMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}
			else if (command.equals("admin_close"))
			{
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2DoorInstance)
				{
					((L2DoorInstance) target).closeMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}
			else if (command.equals("admin_showdoors"))
			{
				for (L2Character ch : activeChar.getKnownList().getKnownCharacters())
				{
					final ExServerPrimitive packet = new ExServerPrimitive("door" + ch.getId(), activeChar.getX(), activeChar.getY(), -16000);
					if (ch.isDoor())
					{
						final L2DoorInstance door = (L2DoorInstance) ch;
						final Color color = door.getOpen() ? Color.GREEN : Color.RED;
						// box 1
						packet.addLine(color, door.getX(0), door.getY(0), door.getZMin(), door.getX(1), door.getY(1), door.getZMin());
						packet.addLine(color, door.getX(1), door.getY(1), door.getZMin(), door.getX(2), door.getY(2), door.getZMax());
						packet.addLine(color, door.getX(2), door.getY(2), door.getZMax(), door.getX(3), door.getY(3), door.getZMax());
						packet.addLine(color, door.getX(3), door.getY(3), door.getZMax(), door.getX(0), door.getY(0), door.getZMin());
						// box 2
						packet.addLine(color, door.getX(0), door.getY(0), door.getZMax(), door.getX(1), door.getY(1), door.getZMax());
						packet.addLine(color, door.getX(1), door.getY(1), door.getZMax(), door.getX(2), door.getY(2), door.getZMin());
						packet.addLine(color, door.getX(2), door.getY(2), door.getZMin(), door.getX(3), door.getY(3), door.getZMin());
						packet.addLine(color, door.getX(3), door.getY(3), door.getZMin(), door.getX(0), door.getY(0), door.getZMax());
						// diagonals
						packet.addLine(color, door.getX(0), door.getY(0), door.getZMin(), door.getX(1), door.getY(1), door.getZMax());
						packet.addLine(color, door.getX(2), door.getY(2), door.getZMin(), door.getX(3), door.getY(3), door.getZMax());
						packet.addLine(color, door.getX(0), door.getY(0), door.getZMax(), door.getX(1), door.getY(1), door.getZMin());
						packet.addLine(color, door.getX(2), door.getY(2), door.getZMax(), door.getX(3), door.getY(3), door.getZMin());
					}
					activeChar.sendPacket(packet);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
