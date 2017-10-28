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
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.ExUISetting;

/**
 * @author KenM / mrTJO
 */
public class RequestKeyMapping extends L2GameClientPacket
{
	private static final String _C__D0_21_REQUESTKEYMAPPING = "[C] D0:21 RequestKeyMapping";
	
	@Override
	protected void readImpl()
	{
		// trigger (no data)
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.STORE_UI_SETTINGS)
		{
			activeChar.sendPacket(new ExUISetting(activeChar));
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
	
	@Override
	public String getType()
	{
		return _C__D0_21_REQUESTKEYMAPPING;
	}
}
