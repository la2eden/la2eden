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

import java.util.List;

import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.model.quest.Quest;
import com.la2eden.gameserver.model.quest.QuestState;

/**
 * @author Tempy
 */
public class GmViewQuestInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	public GmViewQuestInfo(L2PcInstance cha)
	{
		_activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x99);
		writeS(_activeChar.getName());
		
		final List<Quest> questList = _activeChar.getAllActiveQuests();
		
		if (questList.size() == 0)
		{
			writeC(0);
			writeH(0);
			writeH(0);
			return;
		}
		
		writeH(questList.size()); // quest count
		
		for (Quest q : questList)
		{
			writeD(q.getId());
			
			final QuestState qs = _activeChar.getQuestState(q.getName());
			
			if (qs == null)
			{
				writeD(0);
				continue;
			}
			
			writeD(qs.getInt("cond")); // stage of quest progress
		}
	}
}
