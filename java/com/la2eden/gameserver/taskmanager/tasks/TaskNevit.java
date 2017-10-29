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
package com.la2eden.gameserver.taskmanager.tasks;

import com.la2eden.gameserver.model.L2World;
import com.la2eden.gameserver.model.actor.instance.L2PcInstance;
import com.la2eden.gameserver.network.serverpackets.ExNevitAdventTimeChange;
import com.la2eden.gameserver.taskmanager.Task;
import com.la2eden.gameserver.taskmanager.TaskManager;
import com.la2eden.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.la2eden.gameserver.taskmanager.TaskTypes;

/**
 * @author Janiko
 */
public class TaskNevit extends Task
{
	private static final String NAME = "nevit_system";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if ((player == null) || !player.isOnline())
			{
				continue;
			}
			
			player.getVariables().set("hunting_time", 0);
			player.sendPacket(new ExNevitAdventTimeChange(0, true));
		}
		_log.info("Nevit system reseted.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}
