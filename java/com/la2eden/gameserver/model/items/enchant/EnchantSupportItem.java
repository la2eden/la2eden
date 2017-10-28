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
package com.la2eden.gameserver.model.items.enchant;

import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.items.type.EtcItemType;

/**
 * @author UnAfraid
 */
public final class EnchantSupportItem extends AbstractEnchantItem
{
	private final boolean _isWeapon;
	
	public EnchantSupportItem(StatsSet set)
	{
		super(set);
		_isWeapon = getItem().getItemType() == EtcItemType.SCRL_INC_ENCHANT_PROP_WP;
	}
	
	@Override
	public boolean isWeapon()
	{
		return _isWeapon;
	}
}
