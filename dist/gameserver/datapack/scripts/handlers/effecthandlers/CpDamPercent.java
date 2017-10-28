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
package handlers.effecthandlers;

import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.skills.BuffInfo;
import com.la2eden.gameserver.model.stats.Formulas;

/**
 * CP Damage Percent effect implementation.
 * @author Zoey76, Adry_85
 */
public final class CpDamPercent extends AbstractEffect
{
	private final double _power;
	
	public CpDamPercent(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (!info.getEffected().isPlayer())
		{
			return;
		}
		
		if (info.getEffected().isPlayer() && info.getEffected().getActingPlayer().isFakeDeath())
		{
			info.getEffected().stopFakeDeath(true);
		}
		
		final int damage = (int) ((info.getEffected().getCurrentCp() * _power) / 100);
		// Manage attack or cast break of the target (calculating rate, sending message)
		if (!info.getEffected().isRaid() && Formulas.calcAtkBreak(info.getEffected(), damage))
		{
			info.getEffected().breakAttack();
			info.getEffected().breakCast();
		}
		
		if (damage > 0)
		{
			info.getEffected().setCurrentCp(info.getEffected().getCurrentCp() - damage);
		}
	}
}