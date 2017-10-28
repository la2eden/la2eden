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
package com.la2eden.gameserver.model.drops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.la2eden.Config;
import com.la2eden.gameserver.datatables.ItemTable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2RaidBossInstance;
import com.la2eden.gameserver.model.holders.ItemHolder;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.util.Util;
import com.la2eden.util.Rnd;

/**
 * @author NosBit
 */
public class GroupedGeneralDropItem implements IDropItem
{
	private final double _chance;
	private List<GeneralDropItem> _items;
	
	/**
	 * @param chance the chance of this drop item.
	 */
	public GroupedGeneralDropItem(double chance)
	{
		_chance = chance;
	}
	
	protected double getGlobalChanceMultiplier()
	{
		return 1.;
	}
	
	/**
	 * Gets the chance of this drop item.
	 * @return the chance
	 */
	public double getChance()
	{
		return _chance;
	}
	
	/**
	 * Gets the chance of this drop item.
	 * @param victim the victim
	 * @param killer the killer
	 * @return the chance modified by any rates.
	 */
	public double getChance(L2Character victim, L2Character killer)
	{
		for (GeneralDropItem gdi : getItems())
		{
			final L2Item item = ItemTable.getInstance().getTemplate(gdi.getItemId());
			if ((item == null) || !item.hasExImmediateEffect())
			{
				return getChance() * getGlobalChanceMultiplier();
			}
		}
		
		return getChance() * Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
	}
	
	/**
	 * Gets the items.
	 * @return the items
	 */
	public List<GeneralDropItem> getItems()
	{
		return _items;
	}
	
	/**
	 * Sets an item list to this drop item.
	 * @param items the item list
	 */
	public void setItems(List<GeneralDropItem> items)
	{
		_items = Collections.unmodifiableList(items);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.la2eden.gameserver.model.drop.IDropItem#calculateDrops(com.la2eden.gameserver.model.actor.L2Character, com.la2eden.gameserver.model.actor.L2Character)
	 */
	@Override
	public Collection<ItemHolder> calculateDrops(L2Character victim, L2Character killer)
	{
		final int levelDifference = victim.getLevel() - killer.getLevel();
		double chanceModifier;
		if (victim instanceof L2RaidBossInstance)
		{
			chanceModifier = Math.max(0, Math.min(1, (levelDifference * 0.15) + 1));
		}
		else
		{
			chanceModifier = 1;
			if (Util.map(levelDifference, -Config.DROP_ITEM_MAX_LEVEL_DIFFERENCE, -Config.DROP_ITEM_MIN_LEVEL_DIFFERENCE, Config.DROP_ITEM_MIN_LEVEL_GAP_CHANCE, 100.0) < (Rnd.nextDouble() * 100))
			{
				return null;
			}
		}
		
		final double chance = getChance(victim, killer) * chanceModifier;
		final boolean successes = chance > (Rnd.nextDouble() * 100);
		
		if (successes)
		{
			double totalChance = 0;
			final double random = (Rnd.nextDouble() * 100);
			for (GeneralDropItem item : getItems())
			{
				// Grouped item chance rates should not be modified.
				totalChance += item.getChance();
				if (totalChance > random)
				{
					final Collection<ItemHolder> items = new ArrayList<>(1);
					final long baseDropCount = Rnd.get(item.getMin(victim, killer), item.getMax(victim, killer));
					final long finaldropCount = (long) (Config.L2JMOD_OLD_DROP_BEHAVIOR ? (baseDropCount * Math.max(1, chance / 100)) + (chance > 100 && (chance % 100) > (Rnd.nextDouble() * 100) ? baseDropCount : 0) : baseDropCount);
					items.add(new ItemHolder(item.getItemId(), finaldropCount));
					return items;
				}
			}
		}
		
		return null;
	}
}
