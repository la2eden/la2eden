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

import com.la2eden.gameserver.data.sql.impl.ClanTable;
import com.la2eden.gameserver.model.ClanPrivilege;
import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.L2ClanMember;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.ActionFailed;
import com.la2eden.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
	private static final String _C__05_REQUESTSTOPPLEDGEWAR = "[C] 05 RequestStopPledgeWar";
	
	private String _pledgeName;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		final L2Clan playerClan = player.getClan();
		if (playerClan == null)
		{
			return;
		}
		
		final L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		
		if (clan == null)
		{
			player.sendMessage("No such clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!playerClan.isAtWarWith(clan.getId()))
		{
			player.sendMessage("You aren't at war with this clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if player who does the request has the correct rights to do it
		if (!player.hasClanPrivilege(ClanPrivilege.CL_PLEDGE_WAR))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		// _log.info("RequestStopPledgeWar: By leader or authorized player: " + playerClan.getLeaderName() + " of clan: "
		// + playerClan.getName() + " to clan: " + _pledgeName);
		
		// L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
		// if(leader != null && leader.isOnline() == 0)
		// {
		// player.sendMessage("Clan leader isn't online.");
		// player.sendPacket(ActionFailed.STATIC_PACKET);
		// return;
		// }
		
		// if (leader.isProcessingRequest())
		// {
		// SystemMessage sm = SystemMessage.getSystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
		// sm.addString(leader.getName());
		// player.sendPacket(sm);
		// return;
		// }
		
		for (L2ClanMember member : playerClan.getMembers())
		{
			if ((member == null) || (member.getPlayerInstance() == null))
			{
				continue;
			}
			if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(member.getPlayerInstance()))
			{
				player.sendPacket(SystemMessageId.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE);
				return;
			}
		}
		
		ClanTable.getInstance().deleteclanswars(playerClan.getId(), clan.getId());
		
		for (L2PcInstance member : playerClan.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
		
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__05_REQUESTSTOPPLEDGEWAR;
	}
}