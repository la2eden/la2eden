/*
 * This file is part of the La2Eden project.
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

import java.util.ArrayList;
import java.util.List;

import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.NpcStringId;
import com.la2eden.gameserver.network.SystemMessageId;

public final class CreatureSay extends L2GameServerPacket
{
	private final int _objectId;
	private final ChatType _textType;
	private String _charName = null;
	private int _charId = 0;
	private String _text = null;
	private int _npcString = -1;
	private List<String> _parameters;
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, ChatType messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}
	
	public CreatureSay(int objectId, ChatType messageType, int charId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, ChatType messageType, String charName, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, ChatType messageType, int charId, SystemMessageId sysString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = sysString.getId();
	}
	
	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4A);
		writeD(_objectId);
		writeD(_textType.getClientId());
		if (_charName != null)
		{
			writeS(_charName);
		}
		else
		{
			writeD(_charId);
		}
		writeD(_npcString); // High Five NPCString ID
		if (_text != null)
		{
			writeS(_text);
		}
		else if (_parameters != null)
		{
			for (String s : _parameters)
			{
				writeS(s);
			}
		}
	}
	
	@Override
	public final void runImpl()
	{
		final L2PcInstance _pci = getClient().getActiveChar();
		if (_pci != null)
		{
			_pci.broadcastSnoop(_textType, _charName, _text);
		}
	}
}
