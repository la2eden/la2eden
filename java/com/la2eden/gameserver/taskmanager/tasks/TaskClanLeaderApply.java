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
package com.la2eden.gameserver.taskmanager.tasks;

import java.util.Calendar;

import com.la2eden.Config;
import com.la2eden.gameserver.data.sql.impl.ClanTable;
import com.la2eden.gameserver.model.L2Clan;
import com.la2eden.gameserver.model.L2ClanMember;
import com.la2eden.gameserver.taskmanager.Task;
import com.la2eden.gameserver.taskmanager.TaskManager;
import com.la2eden.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.la2eden.gameserver.taskmanager.TaskTypes;

/**
 * @author UnAfraid
 */
public class TaskClanLeaderApply extends Task
{
	private static final String NAME = "clanleaderapply";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Config.ALT_CLAN_LEADER_DATE_CHANGE)
		{
			return;
		}
		
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getNewLeaderId() != 0)
			{
				final L2ClanMember member = clan.getClanMember(clan.getNewLeaderId());
				if (member == null)
				{
					continue;
				}
				
				clan.setNewLeader(member);
			}
		}
		_log.info(getClass().getSimpleName() + ": launched.");
	}
	
	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", Config.ALT_CLAN_LEADER_HOUR_CHANGE, "");
	}
}
