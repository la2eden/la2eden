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

import com.la2eden.gameserver.data.xml.impl.HennaData;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.items.L2Henna;
import com.la2eden.gameserver.network.serverpackets.HennaItemRemoveInfo;

/**
 * @author Zoey76
 */
public final class RequestHennaItemRemoveInfo extends L2GameClientPacket
{
	private static final String _C__71_REQUESTHENNAITEMREMOVEINFO = "[C] 71 RequestHennaItemRemoveInfo";
	
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || (_symbolId == 0))
		{
			return;
		}
		
		final L2Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
		{
			_log.warning(getClass().getName() + ": Invalid Henna Id: " + _symbolId + " from player " + activeChar);
			sendActionFailed();
			return;
		}
		activeChar.sendPacket(new HennaItemRemoveInfo(henna, activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__71_REQUESTHENNAITEMREMOVEINFO;
	}
}
