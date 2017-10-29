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
package handlers.actionhandlers;

import com.la2eden.gameserver.GeoData;
import com.la2eden.gameserver.ai.CtrlIntention;
import com.la2eden.gameserver.enums.InstanceType;
import com.la2eden.gameserver.handler.IActionHandler;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.L2Summon;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.events.EventDispatcher;
import com.la2eden.gameserver.model.events.impl.character.player.OnPlayerSummonTalk;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.network.serverpackets.PetStatusShow;

public class L2SummonAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_TO_CHANGE_ATTACK_TARGET);
			return false;
		}
		
		if ((activeChar == ((L2Summon) target).getOwner()) && (activeChar.getTarget() == target))
		{
			activeChar.sendPacket(new PetStatusShow((L2Summon) target));
			activeChar.updateNotMoveUntil();
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSummonTalk((L2Summon) target), (L2Summon) target);
		}
		else if (activeChar.getTarget() != target)
		{
			activeChar.setTarget(target);
		}
		else if (interact)
		{
			if (target.isAutoAttackable(activeChar))
			{
				if (GeoData.getInstance().canSeeTarget(activeChar, target))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					activeChar.onActionRequest();
				}
			}
			else
			{
				// This Action Failed packet avoids activeChar getting stuck when clicking three or more times
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				if (((L2Summon) target).isInsideRadius(activeChar, 150, false, false))
				{
					activeChar.updateNotMoveUntil();
				}
				else
				{
					if (GeoData.getInstance().canSeeTarget(activeChar, target))
					{
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Summon;
	}
}
