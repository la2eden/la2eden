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

import com.la2eden.gameserver.instancemanager.CHSiegeManager;
import com.la2eden.gameserver.instancemanager.CastleManager;
import com.la2eden.gameserver.model.ClanPrivilege;
import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.entity.Castle;
import com.la2eden.gameserver.model.entity.clanhall.SiegableHall;
import com.la2eden.gameserver.network.SystemMessageId;
import com.la2eden.gameserver.network.serverpackets.SiegeInfo;

/**
 * @author KenM
 */
public final class RequestJoinSiege extends L2GameClientPacket
{
	private static final String _C__AD_RequestJoinSiege = "[C] AD RequestJoinSiege";
	
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.hasClanPrivilege(ClanPrivilege.CS_MANAGE_SIEGE))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle != null)
		{
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION);
					return;
				}
				if (_isAttacker == 1)
				{
					castle.getSiege().registerAttacker(activeChar);
				}
				else
				{
					castle.getSiege().registerDefender(activeChar);
				}
			}
			else
			{
				castle.getSiege().removeSiegeClan(activeChar);
			}
			castle.getSiege().listRegisterClan(activeChar);
		}
		
		final SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(_castleId);
		if (hall != null)
		{
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION);
					return;
				}
				CHSiegeManager.getInstance().registerClan(clan, hall, activeChar);
			}
			else
			{
				CHSiegeManager.getInstance().unRegisterClan(clan, hall);
			}
			activeChar.sendPacket(new SiegeInfo(hall));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__AD_RequestJoinSiege;
	}
}