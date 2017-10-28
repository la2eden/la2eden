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
package com.la2eden.gameserver.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.la2eden.gameserver.enums.PetitionState;
import com.la2eden.gameserver.enums.PetitionType;
import com.la2eden.gameserver.idfactory.IdFactory;
import com.la2eden.gameserver.instancemanager.PetitionManager;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.CreatureSay;
import com.la2eden.gameserver.network.serverpackets.L2GameServerPacket;
import com.la2eden.gameserver.network.serverpackets.PetitionVotePacket;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * Petition
 * @author xban1x
 */
public final class Petition
{
	private final long _submitTime = System.currentTimeMillis();
	private final int _id;
	private final PetitionType _type;
	private PetitionState _state = PetitionState.PENDING;
	private final String _content;
	private final List<CreatureSay> _messageLog = new CopyOnWriteArrayList<>();
	private final L2PcInstance _petitioner;
	private L2PcInstance _responder;
	
	public Petition(L2PcInstance petitioner, String petitionText, int petitionType)
	{
		_id = IdFactory.getInstance().getNextId();
		_type = PetitionType.values()[--petitionType];
		_content = petitionText;
		_petitioner = petitioner;
	}
	
	public boolean addLogMessage(CreatureSay cs)
	{
		return _messageLog.add(cs);
	}
	
	public List<CreatureSay> getLogMessages()
	{
		return _messageLog;
	}
	
	public boolean endPetitionConsultation(PetitionState endState)
	{
		setState(endState);
		
		if ((getResponder() != null) && getResponder().isOnline())
		{
			if (endState == PetitionState.RESPONDER_REJECT)
			{
				getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
			}
			else
			{
				// Ending petition consultation with <Player>.
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ENDING_PETITION_CONSULTATION_WITH_C1);
				sm.addString(getPetitioner().getName());
				getResponder().sendPacket(sm);
				
				if (endState == PetitionState.PETITIONER_CANCEL)
				{
					// Receipt No. <ID> petition cancelled.
					sm = SystemMessage.getSystemMessage(SystemMessageId.RECEIPT_NO_S1_PETITION_CANCELLED);
					sm.addInt(getId());
					getResponder().sendPacket(sm);
				}
			}
		}
		
		// End petition consultation and inform them, if they are still online. And if petitioner is online, enable Evaluation button
		if ((getPetitioner() != null) && getPetitioner().isOnline())
		{
			getPetitioner().sendPacket(SystemMessageId.THIS_ENDS_THE_GM_PETITION_CONSULTATION_NPLEASE_GIVE_US_FEEDBACK_ON_THE_PETITION_SERVICE);
			getPetitioner().sendPacket(PetitionVotePacket.STATIC_PACKET);
		}
		
		PetitionManager.getInstance().getCompletedPetitions().put(getId(), this);
		return PetitionManager.getInstance().getPendingPetitions().remove(getId()) != null;
	}
	
	public String getContent()
	{
		return _content;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public L2PcInstance getPetitioner()
	{
		return _petitioner;
	}
	
	public L2PcInstance getResponder()
	{
		return _responder;
	}
	
	public long getSubmitTime()
	{
		return _submitTime;
	}
	
	public PetitionState getState()
	{
		return _state;
	}
	
	public String getTypeAsString()
	{
		return _type.toString().replace("_", " ");
	}
	
	public void sendPetitionerPacket(L2GameServerPacket responsePacket)
	{
		if ((getPetitioner() == null) || !getPetitioner().isOnline())
		{
			// Allows petitioners to see the results of their petition when
			// they log back into the game.
			
			// endPetitionConsultation(PetitionState.Petitioner_Missing);
			return;
		}
		
		getPetitioner().sendPacket(responsePacket);
	}
	
	public void sendResponderPacket(L2GameServerPacket responsePacket)
	{
		if ((getResponder() == null) || !getResponder().isOnline())
		{
			endPetitionConsultation(PetitionState.RESPONDER_MISSING);
			return;
		}
		
		getResponder().sendPacket(responsePacket);
	}
	
	public void setState(PetitionState state)
	{
		_state = state;
	}
	
	public void setResponder(L2PcInstance respondingAdmin)
	{
		if (getResponder() != null)
		{
			return;
		}
		
		_responder = respondingAdmin;
	}
}
