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

import com.la2eden.gameserver.model.ClanPrivilege;
import com.la2eden.gameserver.model.L2ClanMember;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

public class RequestGiveNickName extends L2GameClientPacket
{
	private static final String _C__0B_REQUESTGIVENICKNAME = "[C] 0B RequestGiveNickName";
	
	private String _target;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Noblesse can bestow a title to themselves
		if (activeChar.isNoble() && _target.equalsIgnoreCase(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(SystemMessageId.YOUR_TITLE_HAS_BEEN_CHANGED);
			activeChar.broadcastTitleInfo();
		}
		else
		{
			// Can the player change/give a title?
			if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_GIVE_TITLE))
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (activeChar.getClan().getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE);
				return;
			}
			
			final L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
			if (member1 != null)
			{
				final L2PcInstance member = member1.getPlayerInstance();
				if (member != null)
				{
					// is target from the same clan?
					member.setTitle(_title);
					member.sendPacket(SystemMessageId.YOUR_TITLE_HAS_BEEN_CHANGED);
					member.broadcastTitleInfo();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0B_REQUESTGIVENICKNAME;
	}
}
