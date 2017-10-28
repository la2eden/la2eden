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

import com.la2eden.gameserver.ai.CtrlEvent;
import com.la2eden.gameserver.enums.Race;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.actor.instance.L2DefenderInstance;
import com.la2eden.gameserver.model.actor.instance.L2FortCommanderInstance;
import com.la2eden.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.EffectFlag;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.skills.BuffInfo;

/**
 * Fear effect implementation.
 * @author littlecrow
 */
public final class Fear extends AbstractEffect
{
	public Fear(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return info.getEffected().isPlayer() || info.getEffected().isSummon() || (info.getEffected().isAttackable() && //
			!((info.getEffected() instanceof L2DefenderInstance) || (info.getEffected() instanceof L2FortCommanderInstance) || //
				(info.getEffected() instanceof L2SiegeFlagInstance) || (info.getEffected().getTemplate().getRace() == Race.SIEGE_WEAPON)));
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}
	
	@Override
	public int getTicks()
	{
		return 5;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		info.getEffected().getAI().notifyEvent(CtrlEvent.EVT_AFRAID, info.getEffector(), false);
		return false;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isCastingNow() && info.getEffected().canAbortCast())
		{
			info.getEffected().abortCast();
		}
		
		info.getEffected().getAI().notifyEvent(CtrlEvent.EVT_AFRAID, info.getEffector(), true);
	}
}
