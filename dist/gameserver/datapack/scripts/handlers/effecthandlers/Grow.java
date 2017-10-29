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
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.skills.BuffInfo;

/**
 * Grow effect implementation.
 */
public final class Grow extends AbstractEffect
{
	public Grow(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (info.getEffected().isNpc())
		{
			final L2Npc npc = (L2Npc) info.getEffected();
			npc.setCollisionRadius(npc.getTemplate().getfCollisionRadius());
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isNpc())
		{
			final L2Npc npc = (L2Npc) info.getEffected();
			npc.setCollisionRadius(npc.getTemplate().getCollisionRadiusGrown());
		}
	}
}
