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
package com.la2eden.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.la2eden.Config;
import com.la2eden.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.la2eden.gameserver.datatables.SkillData;
import com.la2eden.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import com.la2eden.gameserver.model.L2EnchantSkillLearn;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.itemcontainer.Inventory;
import com.la2eden.gameserver.model.items.instance.L2ItemInstance;
import com.la2eden.gameserver.model.skills.Skill;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.la2eden.gameserver.network.serverpackets.ExEnchantSkillInfo;
import com.la2eden.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import com.la2eden.gameserver.network.serverpackets.ExEnchantSkillResult;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;
import com.la2eden.gameserver.network.serverpackets.UserInfo;
import com.la2eden.util.Rnd;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x34 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
{
	private static final String _C__D0_34_REQUESTEXENCHANTSKILLROUTECHANGE = "[C] D0:34 RequestExEnchantSkillRouteChange";
	private static final Logger _logEnchant = Logger.getLogger("enchant");
	
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_CORRESPONDING_FUNCTION_WHEN_COMPLETING_THE_THIRD_CLASS_CHANGE);
			return;
		}
		
		if (player.getLevel() < 76)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL_YOU_CAN_USE_THE_CORRESPONDING_FUNCTION_ON_LEVELS_HIGHER_THAN_76LV);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_THE_SKILL_ENHANCING_FUNCTION_UNDER_OFF_BATTLE_STATUS_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMING_BATTLING_AND_ON_BOARD);
			return;
		}
		
		Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}
		
		final int reqItemId = EnchantSkillGroupsData.CHANGE_ENCHANT_BOOK;
		
		final L2EnchantSkillLearn s = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		
		final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
		// do u have this skill enchanted?
		if (beforeEnchantSkillLevel <= 100)
		{
			return;
		}
		
		final int currentEnchantLevel = beforeEnchantSkillLevel % 100;
		// is the requested level valid?
		if (currentEnchantLevel != (_skillLvl % 100))
		{
			return;
		}
		final EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
		
		final int requiredSp = esd.getSpCost();
		final int requireditems = esd.getAdenaCost();
		
		if (player.getSp() >= requiredSp)
		{
			// only first lvl requires book
			final L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (Config.ES_SP_BOOK_NEEDED)
			{
				if (spb == null)// Haven't spellbook
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_SKILL_ROUTE_CHANGE);
					return;
				}
			}
			
			if (player.getInventory().getAdena() < requireditems)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			boolean check;
			check = player.getStat().removeExpAndSp(0, requiredSp, false);
			if (Config.ES_SP_BOOK_NEEDED)
			{
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			}
			
			check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
			
			if (!check)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			final int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
			_skillLvl -= levelPenalty;
			if ((_skillLvl % 100) == 0)
			{
				_skillLvl = s.getBaseLevel();
			}
			
			skill = SkillData.getInstance().getSkill(_skillId, _skillLvl);
			
			if (skill != null)
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					final LogRecord record = new LogRecord(Level.INFO, "Route Change");
					record.setParameters(new Object[]
					{
						player,
						skill,
						spb
					});
					record.setLoggerName("skill");
					_logEnchant.log(record);
				}
				
				player.addSkill(skill, true);
				player.sendPacket(ExEnchantSkillResult.valueOf(true));
			}
			
			if (Config.DEBUG)
			{
				_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
			}
			
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));
			
			if (levelPenalty == 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_WILL_REMAIN);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_HAS_BEEN_DECREASED_BY_S2);
				sm.addSkillName(_skillId);
				sm.addInt(levelPenalty);
				player.sendPacket(sm);
			}
			player.sendSkillList();
			final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(3, _skillId, afterEnchantSkillLevel, player));
			player.updateShortCuts(_skillId, afterEnchantSkillLevel);
		}
		else
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_34_REQUESTEXENCHANTSKILLROUTECHANGE;
	}
}
