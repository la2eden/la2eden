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
import java.util.Collection;
import java.util.List;

import com.la2eden.gameserver.handler.ITargetTypeHandler;
import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class AuraCorpseMob implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final List<L2Character> targetList = new ArrayList<>();
		// Go through the L2Character _knownList
		final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getAffectRange());
		final int maxTargets = skill.getAffectLimit();
		for (L2Character obj : objs)
		{
			if (obj.isAttackable() && obj.isDead())
			{
				if (onlyFirst)
				{
					return new L2Character[]
					{
						obj
					};
				}
				
				if ((maxTargets > 0) && (targetList.size() >= maxTargets))
				{
					break;
				}
				
				targetList.add(obj);
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.AURA_CORPSE_MOB;
	}
}
