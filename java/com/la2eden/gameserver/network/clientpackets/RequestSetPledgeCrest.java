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

import com.la2eden.gameserver.data.sql.impl.CrestTable;
import com.la2eden.gameserver.model.ClanPrivilege;
import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.L2Crest;
import com.la2eden.gameserver.model.L2Crest.CrestType;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.SystemMessageId;

/**
 * Client packet for setting/deleting clan crest.
 */
public final class RequestSetPledgeCrest extends L2GameClientPacket
{
	private static final String _C__09_REQUESTSETPLEDGECREST = "[C] 09 RequestSetPledgeCrest";
	
	private int _length;
	private byte[] _data = null;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 256)
		{
			return;
		}
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_length < 0))
		{
			activeChar.sendPacket(SystemMessageId.THE_SIZE_OF_THE_UPLOADED_CREST_OR_INSIGNIA_DOES_NOT_MEET_THE_STANDARD_REQUIREMENTS);
			return;
		}
		
		if (_length > 256)
		{
			activeChar.sendPacket(SystemMessageId.THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE_PLEASE_ADJUST_TO_16X12_PIXELS);
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST);
			return;
		}
		
		if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_REGISTER_CREST))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (_length == 0)
		{
			if (clan.getCrestId() != 0)
			{
				clan.changeClanCrest(0);
				activeChar.sendPacket(SystemMessageId.THE_CLAN_S_CREST_HAS_BEEN_DELETED);
			}
		}
		else
		{
			if (clan.getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.A_CLAN_CREST_CAN_ONLY_BE_REGISTERED_WHEN_THE_CLAN_S_SKILL_LEVEL_IS_3_OR_ABOVE);
				return;
			}
			
			final L2Crest crest = CrestTable.getInstance().createCrest(_data, CrestType.PLEDGE);
			if (crest != null)
			{
				clan.changeClanCrest(crest.getId());
				activeChar.sendPacket(SystemMessageId.THE_CREST_WAS_SUCCESSFULLY_REGISTERED);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__09_REQUESTSETPLEDGECREST;
	}
}
