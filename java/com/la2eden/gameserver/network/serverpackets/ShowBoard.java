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

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
	private final String _content;
	private int _showBoard = 1; // 1 show, 0 hide
	
	public ShowBoard(String htmlCode, String id)
	{
		_content = id + "\u0008" + htmlCode;
	}
	
	public ShowBoard(List<String> arg)
	{
		StringBuilder builder = new StringBuilder(256).append("1002\u0008");
		for (String str : arg)
		{
			builder.append(str).append("\u0008");
		}
		_content = builder.toString();
	}
	
	/**
	 * Hides the community board
	 */
	public ShowBoard()
	{
		_showBoard = 0;
		_content = "";
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7B);
		writeC(_showBoard); // c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _bbsmail"); // mail
		writeS("bypass _bbsfriends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		writeS(_content);
	}
}
