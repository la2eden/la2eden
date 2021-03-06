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
package handlers.effecthandlers;

import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.skills.BuffInfo;
import com.la2eden.gameserver.model.zone.ZoneId;

/**
 * NevitsHourglass effect handler.
 * @author St3eT
 */
public final class NevitsHourglass extends AbstractEffect
{
	public NevitsHourglass(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NEVITS_HOURGLASS;
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return (info.getEffected() != null) && info.getEffected().isPlayer();
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		info.getEffected().getActingPlayer().storeRecommendations(true);
		info.getEffected().getActingPlayer().getStat().setPausedNevitHourglassStatus(true);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (!info.getEffected().getActingPlayer().isInsideZone(ZoneId.PEACE))
		{
			info.getEffected().getActingPlayer().getStat().setPausedNevitHourglassStatus(false);
		}
	}
}
