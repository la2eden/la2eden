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
package com.la2eden.gameserver.network.serverpackets;

import com.la2eden.gameserver.enums.PartyDistributionType;

/**
 * @author JIV
 */
public class ExAskModifyPartyLooting extends L2GameServerPacket
{
	private final String _requestor;
	private final PartyDistributionType _partyDistributionType;
	
	public ExAskModifyPartyLooting(String name, PartyDistributionType partyDistributionType)
	{
		_requestor = name;
		_partyDistributionType = partyDistributionType;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xBF);
		writeS(_requestor);
		writeD(_partyDistributionType.getId());
	}
}
