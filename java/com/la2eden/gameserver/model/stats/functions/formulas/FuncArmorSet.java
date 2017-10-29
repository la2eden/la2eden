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

import java.util.HashMap;
import java.util.Map;

import com.la2eden.gameserver.data.xml.impl.ArmorSetsData;
import com.la2eden.gameserver.model.L2ArmorSet;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.model.stats.Stats;
import com.la2eden.gameserver.model.stats.functions.AbstractFunction;

/**
 * @author UnAfraid
 */
public class FuncArmorSet extends AbstractFunction
{
	private static final Map<Stats, FuncArmorSet> _fh_instance = new HashMap<>();
	
	public static AbstractFunction getInstance(Stats st)
	{
		if (!_fh_instance.containsKey(st))
		{
			_fh_instance.put(st, new FuncArmorSet(st));
		}
		return _fh_instance.get(st);
	}
	
	private FuncArmorSet(Stats stat)
	{
		super(stat, 1, null, 0, null);
	}
	
	@Override
	public double calc(L2Character effector, L2Character effected, Skill skill, double initVal)
	{
		double value = initVal;
		final L2PcInstance player = effector.getActingPlayer();
		if (player != null)
		{
			final L2ItemInstance chest = player.getChestArmorInstance();
			if (chest != null)
			{
				final L2ArmorSet set = ArmorSetsData.getInstance().getSet(chest.getId());
				if ((set != null) && set.containAll(player))
				{
					switch (getStat())
					{
						case STAT_STR:
							value += set.getSTR();
							break;
						case STAT_DEX:
							value += set.getDEX();
							break;
						case STAT_INT:
							value += set.getINT();
							break;
						case STAT_MEN:
							value += set.getMEN();
							break;
						case STAT_CON:
							value += set.getCON();
							break;
						case STAT_WIT:
							value += set.getWIT();
							break;
					}
				}
			}
		}
		return value;
	}
}