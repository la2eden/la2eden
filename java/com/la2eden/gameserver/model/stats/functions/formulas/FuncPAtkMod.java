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
package com.la2eden.gameserver.model.stats.functions.formulas;

import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.model.stats.BaseStats;
import com.la2eden.gameserver.model.stats.Stats;
import com.la2eden.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncPAtkMod extends AbstractFunction
{
	private static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();
	
	public static AbstractFunction getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncPAtkMod()
	{
		super(Stats.POWER_ATTACK, 1, null, 0, null);
	}
	
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal)
	{
		return initVal * BaseStats.STR.calcBonus(effector) * effector.getLevelMod();
	}
}