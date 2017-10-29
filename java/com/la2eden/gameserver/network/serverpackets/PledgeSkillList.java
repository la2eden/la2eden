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
package com.la2eden.gameserver.network.serverpackets;

import java.util.List;

import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.skills.Skill;

/**
 * @author -Wooden-
 */
public class PledgeSkillList extends L2GameServerPacket
{
	private final Skill[] _skills;
	private final List<SubPledgeSkill> _subSkills;
	
	public static class SubPledgeSkill
	{
		int _subType;
		int _skillId;
		int _skillLvl;
		
		public SubPledgeSkill(int subType, int skillId, int skillLvl)
		{
			super();
			_subType = subType;
			_skillId = skillId;
			_skillLvl = skillLvl;
		}
	}
	
	public PledgeSkillList(L2Clan clan)
	{
		_skills = clan.getAllSkills();
		_subSkills = clan.getAllSubSkills();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfE);
		writeH(0x3A);
		writeD(_skills.length);
		writeD(_subSkills.size()); // Squad skill length
		for (Skill sk : _skills)
		{
			writeD(sk.getDisplayId());
			writeD(sk.getDisplayLevel());
		}
		for (SubPledgeSkill sk : _subSkills)
		{
			writeD(sk._subType); // Clan Sub-unit types
			writeD(sk._skillId);
			writeD(sk._skillLvl);
		}
	}
}
