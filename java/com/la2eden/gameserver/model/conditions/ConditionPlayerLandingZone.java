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
package com.la2eden.gameserver.model.conditions;

import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.model.zone.ZoneId;

/**
 * The Class ConditionPlayerLandingZone.
 * @author kerberos
 */
public class ConditionPlayerLandingZone extends Condition
{
	private final boolean _val;
	
	/**
	 * Instantiates a new condition player landing zone.
	 * @param val the val
	 */
	public ConditionPlayerLandingZone(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		return effector.isInsideZone(ZoneId.LANDING) == _val;
	}
}
