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

import com.la2eden.gameserver.enums.Race;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.util.Util;

/**
 * The Class ConditionPlayerRace.
 * @author mkizub, Zoey76
 */
public class ConditionPlayerRace extends Condition
{
	private final Race[] _races;
	
	/**
	 * Instantiates a new condition player race.
	 * @param races the list containing the allowed races.
	 */
	public ConditionPlayerRace(Race[] races)
	{
		_races = races;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		return (effector != null) && effector.isPlayer() && Util.contains(_races, effector.getActingPlayer().getRace());
	}
}