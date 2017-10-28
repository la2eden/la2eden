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
package com.la2eden.gameserver.network.clientpackets;

import com.la2eden.Config;
import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.skills.AbnormalType;
import com.la2eden.gameserver.model.skills.Skill;

/**
 * @author KenM
 */
public class RequestDispel extends L2GameClientPacket
{
	private static final String _C_D0_4B_REQUESTDISPEL = "[C] D0:4B RequestDispel";
	
	private int _objectId;
	private int _skillId;
	private int _skillLevel;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_skillId = readD();
		_skillLevel = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLevel <= 0))
		{
			return;
		}
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLevel);
		if (skill == null)
		{
			return;
		}
		if (!skill.canBeDispeled() || skill.isStayAfterDeath() || skill.isDebuff())
		{
			return;
		}
		if (skill.getAbnormalType() == AbnormalType.TRANSFORM)
		{
			return;
		}
		if (skill.isDance() && !Config.DANCE_CANCEL_BUFF)
		{
			return;
		}
		if (activeChar.getObjectId() == _objectId)
		{
			activeChar.stopSkillEffects(true, _skillId);
		}
		else
		{
			if (activeChar.hasSummon() && (activeChar.getSummon().getObjectId() == _objectId))
			{
				activeChar.getSummon().stopSkillEffects(true, _skillId);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C_D0_4B_REQUESTDISPEL;
	}
}
