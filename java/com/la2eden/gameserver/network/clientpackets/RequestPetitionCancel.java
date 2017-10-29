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

import com.la2eden.Config;
import com.la2eden.gameserver.data.xml.impl.AdminData;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.instancemanager.PetitionManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.CreatureSay;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * <p>
 * Format: (c) d
 * <ul>
 * <li>d: Unknown</li>
 * </ul>
 * </p>
 * @author -Wooden-, TempyIncursion
 */
public final class RequestPetitionCancel extends L2GameClientPacket
{
	private static final String _C__8A_REQUEST_PETITIONCANCEL = "[C] 8A RequestPetitionCancel";
	
	// private int _unknown;
	
	@Override
	protected void readImpl()
	{
		// _unknown = readD(); This is pretty much a trigger packet.
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if (activeChar.isGM())
			{
				PetitionManager.getInstance().endActivePetition(activeChar);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.YOUR_PETITION_IS_BEING_PROCESSED);
			}
		}
		else
		{
			if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
			{
				if (PetitionManager.getInstance().cancelActivePetition(activeChar))
				{
					final int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
					
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITION_S_TODAY);
					sm.addString(String.valueOf(numRemaining));
					activeChar.sendPacket(sm);
					
					// Notify all GMs that the player's pending petition has been cancelled.
					final String msgContent = activeChar.getName() + " has canceled a pending petition.";
					AdminData.getInstance().broadcastToGMs(new CreatureSay(activeChar.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.YOU_HAVE_NOT_SUBMITTED_A_PETITION);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__8A_REQUEST_PETITIONCANCEL;
	}
}
