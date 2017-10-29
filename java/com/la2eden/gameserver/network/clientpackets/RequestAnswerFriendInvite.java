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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

import com.la2eden.commons.database.DatabaseFactory;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.FriendPacket;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private static final String _C__78_REQUESTANSWERFRIENDINVITE = "[C] 78 RequestAnswerFriendInvite";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (player.getFriendList().contains(requestor.getObjectId()) //
			|| requestor.getFriendList().contains(player.getObjectId()))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THIS_PLAYER_IS_ALREADY_REGISTERED_ON_YOUR_FRIENDS_LIST);
			sm.addCharName(player);
			requestor.sendPacket(sm);
			return;
		}
		
		if (_response == 1)
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId) VALUES (?, ?), (?, ?)"))
			{
				statement.setInt(1, requestor.getObjectId());
				statement.setInt(2, player.getObjectId());
				statement.setInt(3, player.getObjectId());
				statement.setInt(4, requestor.getObjectId());
				statement.execute();
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST);
				requestor.sendPacket(msg);
				
				// Player added to your friend list
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_FRIENDS_LIST);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				requestor.getFriendList().add(player.getObjectId());
				
				// has joined as friend.
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_AS_A_FRIEND);
				msg.addString(requestor.getName());
				player.sendPacket(msg);
				player.getFriendList().add(requestor.getObjectId());
				
				// Send notifications for both player in order to show them online
				player.sendPacket(new FriendPacket(true, requestor.getObjectId()));
				requestor.sendPacket(new FriendPacket(true, player.getObjectId()));
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not add friend objectid: " + e.getMessage(), e);
			}
		}
		else
		{
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
			requestor.sendPacket(msg);
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__78_REQUESTANSWERFRIENDINVITE;
	}
}
