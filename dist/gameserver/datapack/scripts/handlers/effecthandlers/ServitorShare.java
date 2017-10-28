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

import com.la2eden.gameserver.ThreadPoolManager;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.effects.EffectFlag;
import com.la2eden.gameserver.model.effects.L2EffectType;
import com.la2eden.gameserver.model.skills.BuffInfo;

/**
 * Servitor Share effect implementation.<br>
 * Synchronizing effects on player and servitor if one of them gets removed for some reason the same will happen to another. Partner's effect exit is executed in own thread, since there is no more queue to schedule the effects,<br>
 * partner's effect is called while this effect is still exiting issuing an exit call for the effect, causing a stack over flow.
 * @author UnAfraid, Zoey76
 */
public final class ServitorShare extends AbstractEffect
{
	private static final class ScheduledEffectExitTask implements Runnable
	{
		private final L2Character _effected;
		private final int _skillId;
		
		public ScheduledEffectExitTask(L2Character effected, int skillId)
		{
			_effected = effected;
			_skillId = skillId;
		}
		
		@Override
		public void run()
		{
			_effected.stopSkillEffects(false, _skillId);
		}
	}
	
	public ServitorShare(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final L2Character effected = info.getEffected().isPlayer() ? info.getEffected().getSummon() : info.getEffected().getActingPlayer();
		if (effected != null)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new ScheduledEffectExitTask(effected, info.getSkill().getId()), 100);
		}
	}
}
