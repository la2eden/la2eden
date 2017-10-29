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

import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.instancemanager.FortSiegeManager;
import com.la2eden.gameserver.instancemanager.MercTicketManager;
import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PetInstance;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;

public final class RequestPetGetItem extends L2GameClientPacket
{
	private static final String _C__98_REQUESTPETGETITEM = "[C] 98 RequestPetGetItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2World world = L2World.getInstance();
		final L2ItemInstance item = (L2ItemInstance) world.findObject(_objectId);
		if ((item == null) || (getActiveChar() == null) || !getActiveChar().hasPet())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int castleId = MercTicketManager.getInstance().getTicketCastleId(item.getId());
		if (castleId > 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (FortSiegeManager.getInstance().isCombat(item.getId()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) getClient().getActiveChar().getSummon();
		if (pet.isDead() || pet.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (pet.isUncontrollable())
		{
			sendPacket(SystemMessageId.YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM);
			return;
		}
		
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
	
	@Override
	public String getType()
	{
		return _C__98_REQUESTPETGETITEM;
	}
}
