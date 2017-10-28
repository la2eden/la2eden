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
import com.la2eden.gameserver.handler.CommunityBoardHandler;

/**
 * RequestShowBoard client packet implementation.
 * @author Zoey76
 */
public final class RequestShowBoard extends L2GameClientPacket
{
	private static final String _C__5E_REQUESTSHOWBOARD = "[C] 5E RequestShowBoard";
	
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected final void readImpl()
	{
		_unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		CommunityBoardHandler.getInstance().handleParseCommand(Config.BBS_DEFAULT, getActiveChar());
	}
	
	@Override
	public final String getType()
	{
		return _C__5E_REQUESTSHOWBOARD;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
