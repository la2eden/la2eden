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
package handlers.bypasshandlers;

import com.la2eden.gameserver.handler.IBypassHandler;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;

public class Link implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Link"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		final String htmlPath = command.substring(4).trim();
		if (htmlPath.isEmpty())
		{
			_log.warning("Player " + activeChar.getName() + " sent empty link html!");
			return false;
		}
		
		if (htmlPath.contains(".."))
		{
			_log.warning("Player " + activeChar.getName() + " sent invalid link html: " + htmlPath);
			return false;
		}
		
		final String filename = "./datapack/html/" + htmlPath;
		final NpcHtmlMessage html = new NpcHtmlMessage(target != null ? target.getObjectId() : 0);
		html.setFile(activeChar.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(target != null ? target.getObjectId() : 0));
		activeChar.sendPacket(html);
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
