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

import com.la2eden.gameserver.model.L2Party;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;

/**
 * This packet is received from client when a party leader requests to change the leadership to another player in his party.
 */
public final class RequestChangePartyLeader extends L2GameClientPacket
{
	private static final String _C__D0_0C_REQUESTCHANGEPARTYLEADER = "[C] D0:0C RequestChangePartyLeader";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2Party party = activeChar.getParty();
		if ((party != null) && party.isLeader(activeChar))
		{
			party.changePartyLeader(_name);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0C_REQUESTCHANGEPARTYLEADER;
	}
}
