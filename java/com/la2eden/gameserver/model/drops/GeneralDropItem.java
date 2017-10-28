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

import com.la2eden.Config;
import com.la2eden.gameserver.datatables.ItemTable;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.actor.instance.L2ChestInstance;
import com.la2eden.gameserver.model.holders.ItemHolder;
import com.la2eden.gameserver.model.itemcontainer.Inventory;
import com.la2eden.gameserver.model.items.L2Item;
import com.la2eden.gameserver.util.Util;
import com.la2eden.util.Rnd;

/**
 * @author NosBit
 */
public class GeneralDropItem implements IDropItem
{
	private final int _itemId;
	private final long _min;
	private final long _max;
	private final double _chance;
	
	/**
	 * @param itemId the item id
	 * @param min the min count
	 * @param max the max count
	 * @param chance the chance of this drop item
	 */
	public GeneralDropItem(int itemId, long min, long max, double chance)
	{
		_itemId = itemId;
		_min = min;
		_max = max;
		_chance = chance;
	}
	
	protected double getGlobalChanceMultiplier(boolean isPremium)
	{
		return 1.;
	}
	
	protected double getGlobalAmountMultiplier(boolean isPremium)
	{
		return 1.;
	}
	
	private final long getMinMax(L2Character victim, L2Character killer, long val)
	{
		// Treasure chests x1
		if (victim instanceof L2ChestInstance)
		{
			return val;
		}
		
		double multiplier = 1;
		
		// individual drop amount
		Float individualDropAmountMultiplier = null;
		if (killer.getActingPlayer().hasPremiumStatus())
		{
			final Float normalMultiplier = Config.RATE_DROP_AMOUNT_BY_ID.get(getItemId());
			final Float premiumMultiplier = Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(getItemId());
			if ((normalMultiplier != null) && (premiumMultiplier != null))
			{
				individualDropAmountMultiplier = normalMultiplier * premiumMultiplier;
			}
			else if (normalMultiplier != null)
			{
				individualDropAmountMultiplier = normalMultiplier;
			}
			else if (premiumMultiplier != null)
			{
				individualDropAmountMultiplier = premiumMultiplier;
			}
		}
		else
		{
			individualDropAmountMultiplier = Config.RATE_DROP_AMOUNT_BY_ID.get(getItemId());
		}
		
		if (individualDropAmountMultiplier != null)
		{
			// individual amount list multiplier
			multiplier *= individualDropAmountMultiplier;
		}
		else
		{
			final L2Item item = ItemTable.getInstance().getTemplate(getItemId());
			// global amount multiplier
			if ((item != null) && item.hasExImmediateEffect())
			{
				// global herb amount multiplier
				multiplier *= Config.RATE_HERB_DROP_AMOUNT_MULTIPLIER;
			}
			else if (victim.isRaid())
			{
				// global raid amount multiplier
				multiplier *= Config.RATE_RAID_DROP_AMOUNT_MULTIPLIER;
			}
			else
			{
				// drop type specific amount multiplier
				multiplier *= getGlobalAmountMultiplier(killer.getActingPlayer().hasPremiumStatus());
			}
		}
		
		// global champions amount multiplier
		if (victim.isChampion())
		{
			multiplier *= getItemId() != Inventory.ADENA_ID ? Config.L2JMOD_CHAMPION_REWARDS_AMOUNT : Config.L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT;
		}
		
		return (long) (val * multiplier);
	}
	
	/**
	 * Gets the item id
	 * @return the item id
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Gets the min drop count
	 * @return the min
	 */
	public long getMin()
	{
		return _min;
	}
	
	/**
	 * Gets the min drop count
	 * @param victim the victim
	 * @param killer the killer
	 * @return the min modified by any rates.
	 */
	public long getMin(L2Character victim, L2Character killer)
	{
		return getMinMax(victim, killer, getMin());
	}
	
	/**
	 * Gets the max drop count
	 * @return the max
	 */
	public long getMax()
	{
		return _max;
	}
	
	/**
	 * Gets the max drop count
	 * @param victim the victim
	 * @param killer the killer
	 * @return the max modified by any rates.
	 */
	public long getMax(L2Character victim, L2Character killer)
	{
		return getMinMax(victim, killer, getMax());
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
		// Treasure chests x1
		if (victim instanceof L2ChestInstance)
		{
			return getChance();
		}
		
		double multiplier = 1;
		
		// individual drop chance
		Float individualDropChanceMultiplier = null;
		if (killer.getActingPlayer().hasPremiumStatus())
		{
			final Float normalMultiplier = Config.RATE_DROP_CHANCE_BY_ID.get(getItemId());
			final Float premiumMultiplier = Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(getItemId());
			if ((normalMultiplier != null) && (premiumMultiplier != null))
			{
				individualDropChanceMultiplier = normalMultiplier * premiumMultiplier;
			}
			else if (normalMultiplier != null)
			{
				individualDropChanceMultiplier = normalMultiplier;
			}
			else if (premiumMultiplier != null)
			{
				individualDropChanceMultiplier = premiumMultiplier;
			}
		}
		else
		{
			individualDropChanceMultiplier = Config.RATE_DROP_CHANCE_BY_ID.get(getItemId());
		}
		
		if (individualDropChanceMultiplier != null)
		{
			multiplier *= individualDropChanceMultiplier;
		}
		else
		{
			final L2Item item = ItemTable.getInstance().getTemplate(getItemId());
			if ((item != null) && item.hasExImmediateEffect())
			{
				multiplier *= Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
			}
			else if (victim.isRaid())
			{
				// global raid chance multiplier
				multiplier *= Config.RATE_RAID_DROP_CHANCE_MULTIPLIER;
			}
			else
			{
				multiplier *= getGlobalChanceMultiplier(killer.getActingPlayer().hasPremiumStatus());
			}
		}
		
		if (victim.isChampion())
		{
			multiplier *= getItemId() != Inventory.ADENA_ID ? Config.L2JMOD_CHAMPION_REWARDS_CHANCE : Config.L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE;
		}
		
		return (getChance() * multiplier);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.la2eden.gameserver.model.drop.IDropItem#calculateDrops(com.la2eden.gameserver.model.actor.L2Character, com.la2eden.gameserver.model.actor.L2Character)
	 */
	@Override
	public Collection<ItemHolder> calculateDrops(L2Character victim, L2Character killer)
	{
		final int levelDifference = victim.getLevel() - killer.getLevel();
		final double levelGapChanceToDrop;
		if (getItemId() == Inventory.ADENA_ID)
		{
			levelGapChanceToDrop = Util.map(levelDifference, -Config.DROP_ADENA_MAX_LEVEL_DIFFERENCE, -Config.DROP_ADENA_MIN_LEVEL_DIFFERENCE, Config.DROP_ADENA_MIN_LEVEL_GAP_CHANCE, 100.0);
		}
		else
		{
			levelGapChanceToDrop = Util.map(levelDifference, -Config.DROP_ITEM_MAX_LEVEL_DIFFERENCE, -Config.DROP_ITEM_MIN_LEVEL_DIFFERENCE, Config.DROP_ITEM_MIN_LEVEL_GAP_CHANCE, 100.0);
		}
		
		// There is a chance of level gap that it wont drop this item
		if (levelGapChanceToDrop < (Rnd.nextDouble() * 100))
		{
			return null;
		}
		
		final double chance = getChance(victim, killer);
		final boolean successes = chance > (Rnd.nextDouble() * 100);
		if (successes)
		{
			final Collection<ItemHolder> items = new ArrayList<>(1);
			final long baseDropCount = Rnd.get(getMin(victim, killer), getMax(victim, killer));
			final long finaldropCount = (long) (Config.L2JMOD_OLD_DROP_BEHAVIOR ? (baseDropCount * Math.max(1, chance / 100)) + (chance > 100 ? (chance % 100) > (Rnd.nextDouble() * 100) ? baseDropCount : 0 : 0) : baseDropCount);
			items.add(new ItemHolder(getItemId(), finaldropCount));
			return items;
		}
		
		return null;
	}
}
