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
package com.la2eden.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.la2eden.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.la2eden.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import com.la2eden.gameserver.model.L2EnchantSkillLearn;

public final class ExEnchantSkillInfo extends L2GameServerPacket
{
	private final List<Integer> _routes = new ArrayList<>(); // skill lvls for each route
	
	private final int _id;
	private final int _lvl;
	private boolean _maxEnchanted = false;
	
	public ExEnchantSkillInfo(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
		
		final L2EnchantSkillLearn enchantLearn = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_id);
		// do we have this skill?
		if (enchantLearn != null)
		{
			// skill already enchanted?
			if (_lvl > 100)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(_lvl);
				
				// get detail for next level
				final EnchantSkillHolder esd = enchantLearn.getEnchantSkillHolder(_lvl);
				
				// if it exists add it
				if (esd != null)
				{
					_routes.add(_lvl); // current enchant add firts
				}
				
				final int skillLvL = (_lvl % 100);
				
				for (int route : enchantLearn.getAllRoutes())
				{
					if (((route * 100) + skillLvL) == _lvl)
					{
						continue;
					}
					// add other levels of all routes - same lvl as enchanted
					// lvl
					_routes.add((route * 100) + skillLvL);
				}
				
			}
			else
			// not already enchanted
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					_routes.add((route * 100) + 1);
				}
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2a);
		writeD(_id);
		writeD(_lvl);
		writeD(_maxEnchanted ? 0 : 1);
		writeD(_lvl > 100 ? 1 : 0); // enchanted?
		writeD(_routes.size());
		
		for (int level : _routes)
		{
			writeD(level);
		}
	}
}