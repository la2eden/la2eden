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
package handlers.itemhandlers;

import com.la2eden.gameserver.handler.IItemHandler;
import com.la2eden.gameserver.model.actor.L2Playable;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.model.zone.ZoneId;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.Dice;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.gameserver.util.Broadcast;
import com.la2eden.util.Rnd;

public class RollingDice implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final int itemId = item.getId();
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_MATCH);
			return false;
		}
		
		final int number = rollDice(activeChar);
		if (number == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
			return false;
		}
		
		Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), itemId, number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_ROLLED_A_S2);
		sm.addString(activeChar.getName());
		sm.addInt(number);
		
		activeChar.sendPacket(sm);
		if (activeChar.isInsideZone(ZoneId.PEACE))
		{
			Broadcast.toKnownPlayers(activeChar, sm);
		}
		else if (activeChar.isInParty()) // TODO: Verify this!
		{
			activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
		}
		return true;
		
	}
	
	/**
	 * @param player
	 * @return
	 */
	private int rollDice(L2PcInstance player)
	{
		// Check if the dice is ready
		if (!player.getFloodProtectors().getRollDice().tryPerformAction("roll dice"))
		{
			return 0;
		}
		return Rnd.get(1, 6);
	}
}
