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
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

public final class RequestAnswerJoinAlly extends L2GameClientPacket
{
	private static final String _C__8D_REQUESTANSWERJOINALLY = "[C] 8D RequestAnswerJoinAlly";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance requestor = activeChar.getRequest().getPartner();
		if (requestor == null)
		{
			return;
		}
		
		if (_response == 0)
		{
			activeChar.sendPacket(SystemMessageId.NO_RESPONSE_YOUR_ENTRANCE_TO_THE_ALLIANCE_HAS_BEEN_CANCELLED);
			requestor.sendPacket(SystemMessageId.NO_RESPONSE_INVITATION_TO_JOIN_AN_ALLIANCE_HAS_BEEN_CANCELLED);
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly))
			{
				return; // hax
			}
			
			final L2Clan clan = requestor.getClan();
			// we must double check this cause of hack
			if (clan.checkAllyJoinCondition(requestor, activeChar))
			{
				// TODO: Need correct message id
				requestor.sendPacket(SystemMessageId.THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST);
				activeChar.sendPacket(SystemMessageId.YOU_HAVE_ACCEPTED_THE_ALLIANCE);
				
				activeChar.getClan().setAllyId(clan.getAllyId());
				activeChar.getClan().setAllyName(clan.getAllyName());
				activeChar.getClan().setAllyPenaltyExpiryTime(0, 0);
				activeChar.getClan().changeAllyCrest(clan.getAllyCrestId(), true);
				activeChar.getClan().updateClanInDB();
			}
		}
		
		activeChar.getRequest().onRequestResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__8D_REQUESTANSWERJOINALLY;
	}
}
