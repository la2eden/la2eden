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
package com.la2eden.gameserver.model.events.impl.character;

import com.la2eden.gameserver.model.L2Object;
import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.events.EventType;
import com.la2eden.gameserver.model.events.impl.IBaseEvent;
import com.la2eden.gameserver.model.skills.Skill;

/**
 * An instantly executed event when L2Character is attacked by L2Character.
 * @author UnAfraid
 */
public class OnCreatureSkillUse implements IBaseEvent
{
	private final L2Character _caster;
	private final Skill _skill;
	private final boolean _simultaneously;
	private final L2Character _target;
	private final L2Object[] _targets;
	
	public OnCreatureSkillUse(L2Character caster, Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		_caster = caster;
		_skill = skill;
		_simultaneously = simultaneously;
		_target = target;
		_targets = targets;
	}
	
	public final L2Character getCaster()
	{
		return _caster;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public boolean isSimultaneously()
	{
		return _simultaneously;
	}
	
	public final L2Character getTarget()
	{
		return _target;
	}
	
	public L2Object[] getTargets()
	{
		return _targets;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_SKILL_USE;
	}
}