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

import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.model.StatsSet;
import com.la2eden.gameserver.model.conditions.Condition;
import com.la2eden.gameserver.model.effects.AbstractEffect;
import com.la2eden.gameserver.model.skills.BuffInfo;
import com.la2eden.gameserver.model.skills.Skill;

/**
 * Set Skill effect implementation.
 * @author Zoey76
 */
public final class SetSkill extends AbstractEffect
{
	private final int _skillId;
	private final int _skillLvl;
	
	public SetSkill(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_skillId = params.getInt("skillId", 0);
		_skillLvl = params.getInt("skillLvl", 1);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if ((info.getEffected() == null) || !info.getEffected().isPlayer())
		{
			return;
		}
		
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}
		
		info.getEffected().getActingPlayer().addSkill(skill, true);
		info.getEffected().getActingPlayer().sendSkillList();
	}
}
