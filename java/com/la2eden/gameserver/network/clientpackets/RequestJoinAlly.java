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

import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.AskJoinAlly;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestJoinAlly extends L2GameClientPacket
{
	private static final String _C__8C_REQUESTJOINALLY = "[C] 8C RequestJoinAlly";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance ob = L2World.getInstance().getPlayer(_id);
		
		if (ob == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
			return;
		}
		
		final L2PcInstance target = ob;
		final L2Clan clan = activeChar.getClan();
		if (!clan.checkAllyJoinCondition(activeChar, target))
		{
			return;
		}
		if (!activeChar.getRequest().setRequest(target, this))
		{
			return;
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_LEADER_S2_HAS_REQUESTED_AN_ALLIANCE);
		sm.addString(activeChar.getClan().getAllyName());
		sm.addString(activeChar.getName());
		target.sendPacket(sm);
		target.sendPacket(new AskJoinAlly(activeChar.getObjectId(), activeChar.getClan().getAllyName()));
	}
	
	@Override
	public String getType()
	{
		return _C__8C_REQUESTJOINALLY;
	}
}
