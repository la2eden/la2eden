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

import com.la2eden.Config;
import com.la2eden.gameserver.enums.PartyDistributionType;
import com.la2eden.gameserver.model.BlockList;
import com.la2eden.gameserver.model.L2Party;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.AskJoinParty;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * sample 29 42 00 00 10 01 00 00 00 format cdd
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestJoinParty extends L2GameClientPacket
{
	private static final String _C__42_REQUESTJOINPARTY = "[C] 42 RequestJoinParty";
	
	private String _name;
	private int _partyDistributionTypeId;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_partyDistributionTypeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance requestor = getClient().getActiveChar();
		final L2PcInstance target = L2World.getInstance().getPlayer(_name);
		
		if (requestor == null)
		{
			return;
		}
		
		if (target == null)
		{
			requestor.sendPacket(SystemMessageId.YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE_TO_YOUR_PARTY);
			return;
		}
		
		if ((target.getClient() == null) || target.getClient().isDetached())
		{
			requestor.sendMessage("Player is in offline mode.");
			return;
		}
		
		if (requestor.isPartyBanned())
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_PARTICIPATING_IN_A_PARTY_IS_NOT_ALLOWED);
			requestor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (target.isPartyBanned())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_AND_CANNOT_JOIN_A_PARTY);
			sm.addCharName(target);
			requestor.sendPacket(sm);
			return;
		}
		
		if (!target.isVisibleFor(requestor))
		{
			requestor.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}
		
		SystemMessage sm;
		if (target.isInParty())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			return;
		}
		
		if (BlockList.isBlocked(target, requestor))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST);
			sm.addCharName(target);
			requestor.sendPacket(sm);
			return;
		}
		
		if (target == requestor)
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
		{
			requestor.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (target.isJailed() || requestor.isJailed())
		{
			requestor.sendMessage("You cannot invite a player while is in Jail.");
			return;
		}
		
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
		{
			if ((target.isInOlympiadMode() != requestor.isInOlympiadMode()) || (target.getOlympiadGameId() != requestor.getOlympiadGameId()) || (target.getOlympiadSide() != requestor.getOlympiadSide()))
			{
				requestor.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
				return;
			}
		}
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_INVITED_TO_THE_PARTY);
		sm.addCharName(target);
		requestor.sendPacket(sm);
		
		if (!requestor.isInParty())
		{
			createNewParty(target, requestor);
		}
		else
		{
			if (requestor.getParty().isInDimensionalRift())
			{
				requestor.sendMessage("You cannot invite a player when you are in the Dimensional Rift.");
			}
			else
			{
				addTargetToParty(target, requestor);
			}
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		final L2Party party = requestor.getParty();
		// summary of ppl already in party and ppl that get invitation
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
			return;
		}
		if (party.getMemberCount() >= 9)
		{
			requestor.sendPacket(SystemMessageId.THE_PARTY_IS_FULL);
			return;
		}
		if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			// in case a leader change has happened, use party's mode
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getDistributionType()));
			party.setPendingInvitation(true);
			
			if (Config.DEBUG)
			{
				_log.fine("sent out a party invitation to:" + target.getName());
			}
			
		}
		else
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			
			if (Config.DEBUG)
			{
				_log.warning(requestor.getName() + " already received a party invitation");
			}
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		final PartyDistributionType partyDistributionType = PartyDistributionType.findById(_partyDistributionTypeId);
		if (partyDistributionType == null)
		{
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			target.sendPacket(new AskJoinParty(requestor.getName(), partyDistributionType));
			target.setActiveRequester(requestor);
			requestor.onTransactionRequest(target);
			requestor.setPartyDistributionType(partyDistributionType);
			
			if (Config.DEBUG)
			{
				_log.fine("sent out a party invitation to:" + target.getName());
			}
			
		}
		else
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			
			if (Config.DEBUG)
			{
				_log.warning(requestor.getName() + " already received a party invitation");
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__42_REQUESTJOINPARTY;
	}
}
