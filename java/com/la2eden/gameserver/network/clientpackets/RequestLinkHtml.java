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

package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.NpcHtmlMessage;
import com.la2eden.gameserver.util.Util;

/**
 * Lets drink to code!
 * @author zabbix, HorridoJoho
 */
public final class RequestLinkHtml extends L2GameClientPacket
{
	private static final String _C__22_REQUESTLINKHTML = "[C] 22 RequestLinkHtml";
	private String _link;
	
	@Override
	protected void readImpl()
	{
		_link = readS();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		if (_link.isEmpty())
		{
			_log.warning("Player " + actor.getName() + " sent empty html link!");
			return;
		}
		
		if (_link.contains(".."))
		{
			_log.warning("Player " + actor.getName() + " sent invalid html link: link " + _link);
			return;
		}
		
		final int htmlObjectId = actor.validateHtmlAction("link " + _link);
		if (htmlObjectId == -1)
		{
			_log.warning("Player " + actor.getName() + " sent non cached  html link: link " + _link);
			return;
		}
		
		if ((htmlObjectId > 0) && !Util.isInsideRangeOfObjectId(actor, htmlObjectId, L2Npc.INTERACTION_DISTANCE))
		{
			// No logging here, this could be a common case
			return;
		}
		
		final String filename = "./datapack/html/" + _link;
		final NpcHtmlMessage msg = new NpcHtmlMessage(htmlObjectId);
		msg.setFile(actor.getHtmlPrefix(), filename);
		sendPacket(msg);
	}
	
	@Override
	public String getType()
	{
		return _C__22_REQUESTLINKHTML;
	}
}
