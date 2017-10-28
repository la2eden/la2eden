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
package handlers.targethandlers;

import java.util.ArrayList;
import java.util.List;

import com.la2eden.gameserver.handler.ITargetTypeHandler;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2DoorInstance;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.model.skills.targets.L2TargetType;
import com.la2eden.gameserver.model.zone.ZoneId;

/**
 * Aura target handler.
 * @author UnAfraid
 */
public class Aura implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final List<L2Character> targetList = new ArrayList<>();
		final boolean srcInArena = (activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE));
		for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(skill.getAffectRange()))
		{
			if (obj.isDoor() || obj.isAttackable() || obj.isPlayable())
			{
				// Stealth door targeting.
				if (obj.isDoor())
				{
					final L2DoorInstance door = (L2DoorInstance) obj;
					if (!door.getTemplate().isStealth())
					{
						continue;
					}
				}
				
				if (!Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
				{
					continue;
				}
				
				if (activeChar.isPlayable() && obj.isAttackable() && !skill.isBad())
				{
					continue;
				}
				
				if (onlyFirst)
				{
					return new L2Character[]
					{
						obj
					};
				}
				
				targetList.add(obj);
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.AURA;
	}
}
