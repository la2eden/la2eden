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

import java.util.concurrent.TimeUnit;

import com.la2eden.Config;
import com.la2eden.gameserver.model.ClanPrivilege;
import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.L2ClanMember;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.la2eden.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestOustPledgeMember extends L2GameClientPacket
{
	private static final String _C__29_REQUESTOUSTPLEDGEMEMBER = "[C] 29 RequestOustPledgeMember";
	
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_target = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
			return;
		}
		if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_DISMISS))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (activeChar.getName().equalsIgnoreCase(_target))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF);
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		
		final L2ClanMember member = clan.getClanMember(_target);
		if (member == null)
		{
			_log.warning("Target (" + _target + ") is not member of the clan");
			return;
		}
		if (member.isOnline() && member.getPlayerInstance().isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}
		
		// this also updates the database
		clan.removeClanMember(member.getObjectId(), System.currentTimeMillis() + TimeUnit.DAYS.toMillis(Config.ALT_CLAN_JOIN_DAYS));
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(Config.ALT_CLAN_JOIN_DAYS));
		clan.updateClanInDB();
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED);
		sm.addString(member.getName());
		clan.broadcastToOnlineMembers(sm);
		activeChar.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_THE_CLAN_MEMBER);
		activeChar.sendPacket(SystemMessageId.AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER);
		
		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));
		
		if (member.isOnline())
		{
			final L2PcInstance player = member.getPlayerInstance();
			player.sendPacket(SystemMessageId.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__29_REQUESTOUSTPLEDGEMEMBER;
	}
}
