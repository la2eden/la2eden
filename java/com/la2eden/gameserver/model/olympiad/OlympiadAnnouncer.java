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
package com.la2eden.gameserver.model.olympiad;

import java.util.Set;

import com.la2eden.gameserver.datatables.SpawnTable;
import com.la2eden.gameserver.enums.ChatType;
import com.la2eden.gameserver.model.L2Spawn;
import com.la2eden.gameserver.model.actor.L2Npc;
import com.la2eden.gameserver.network.NpcStringId;
import com.la2eden.gameserver.network.serverpackets.NpcSay;

/**
 * @author DS
 */
public final class OlympiadAnnouncer implements Runnable
{
	private static final int OLY_MANAGER = 31688;
	private final Set<L2Spawn> _managers;
	private int _currentStadium = 0;
	
	public OlympiadAnnouncer()
	{
		_managers = SpawnTable.getInstance().getSpawns(OLY_MANAGER);
	}
	
	@Override
	public void run()
	{
		OlympiadGameTask task;
		for (int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0; _currentStadium++)
		{
			if (_currentStadium >= OlympiadGameManager.getInstance().getNumberOfStadiums())
			{
				_currentStadium = 0;
			}
			
			task = OlympiadGameManager.getInstance().getOlympiadTask(_currentStadium);
			if ((task != null) && (task.getGame() != null) && task.needAnnounce())
			{
				NpcStringId npcString;
				final String arenaId = String.valueOf(task.getGame().getStadiumId() + 1);
				switch (task.getGame().getType())
				{
					case NON_CLASSED:
					{
						npcString = NpcStringId.OLYMPIAD_CLASS_FREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
						break;
					}
					case CLASSED:
					{
						npcString = NpcStringId.OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
						break;
					}
					case TEAMS:
					{
						npcString = NpcStringId.OLYMPIAD_CLASS_FREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
						break;
					}
					default:
					{
						continue;
					}
				}
				
				L2Npc manager;
				NpcSay packet;
				for (L2Spawn spawn : _managers)
				{
					manager = spawn.getLastSpawn();
					if (manager != null)
					{
						packet = new NpcSay(manager.getObjectId(), ChatType.NPC_SHOUT, manager.getId(), npcString);
						packet.addStringParameter(arenaId);
						manager.broadcastPacket(packet);
					}
				}
				break;
			}
		}
	}
}
