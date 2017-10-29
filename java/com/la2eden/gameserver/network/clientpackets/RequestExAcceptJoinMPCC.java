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
package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.gameserver.model.L2CommandChannel;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * format: (ch) d
 * @author -Wooden-
 */
public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private static final String _C__D0_07_REQUESTEXASKJOINMPCC = "[C] D0:07 RequestExAcceptJoinMPCC";
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			final L2PcInstance requestor = player.getActiveRequester();
			SystemMessage sm;
			if (requestor == null)
			{
				return;
			}
			
			if (_response == 1)
			{
				boolean newCc = false;
				if (!requestor.getParty().isInCommandChannel())
				{
					new L2CommandChannel(requestor); // Create new CC
					sm = SystemMessage.getSystemMessage(SystemMessageId.THE_COMMAND_CHANNEL_HAS_BEEN_FORMED);
					requestor.sendPacket(sm);
					newCc = true;
				}
				requestor.getParty().getCommandChannel().addParty(player.getParty());
				if (!newCc)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_JOINED_THE_COMMAND_CHANNEL);
					player.sendPacket(sm);
				}
			}
			else
			{
				requestor.sendMessage("The player declined to join your Command Channel.");
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
		
	}
	
	@Override
	public String getType()
	{
		return _C__D0_07_REQUESTEXASKJOINMPCC;
	}
}
